package net.supersonic.voice;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Runs BEFORE Minecraft starts (Fabric "preLaunch" entrypoint).
 *
 * Why this exists: on Android launchers (PojavLauncher etc.) Minecraft itself touches JNA very early
 * (the crash-report / system-details code uses OSHI, and OSHI uses JNA). If JNA starts with its default
 * settings it unpacks libjnidispatch.so into shared storage (/storage/emulated/0/...), Android refuses to
 * load native code from there, and com.sun.jna.Native is then permanently broken for the whole session
 * ("NoClassDefFoundError: Could not initialize class com.sun.jna.Native").
 *
 * Setting the JNA properties later (when the microphone is switched on) is too late, so they are set here.
 *
 * IMPORTANT: this class must not touch JNA, Vosk or any Minecraft class.
 */
public final class JnaPreLaunch implements PreLaunchEntrypoint {
    private static File dirCache = null;

    /** True when libjnidispatch.so for Android was on disk before Minecraft started (JNA can use it from the start). */
    public static volatile boolean jnaReadyAtStart = false;

    @Override
    public void onPreLaunch() {
        try {
            if (!isAndroidLike()) return;
            File dir = privateNativesDir();
            dir.mkdirs();
            String path = dir.getAbsolutePath();
            // JNA unpacks/loads its own native library from here (private app storage = allowed to run native code)
            System.setProperty("jna.tmpdir", path);
            System.setProperty("jna.boot.library.path", path);
            // JNA must find an Android-compatible libjnidispatch.so right now. If it is missing, JNA unpacks its
            // own Linux (glibc) build, which cannot run on Android, and JNA stays broken until the game restarts.
            jnaReadyAtStart = ensureJnaNative(dir);
        } catch (Throwable ignored) {
            // must never stop the game from starting
        }
    }

    /** Downloads libjnidispatch.so for this CPU from Maven Central when it is not on disk yet. Fast fail when offline. */
    private static boolean ensureJnaNative(File dir) {
        File target = new File(dir, "libjnidispatch.so");
        if (target.isFile() && target.length() > 0L) return true;
        try {
            String v = jnaVersion();
            String url = "https://repo1.maven.org/maven2/net/java/dev/jna/jna/" + v + "/jna-" + v + ".aar";
            String entryName = "jni/" + androidAbi() + "/libjnidispatch.so";
            HttpURLConnection c = (HttpURLConnection) URI.create(url).toURL().openConnection();
            c.setConnectTimeout(8000);
            c.setReadTimeout(20000);
            c.setInstanceFollowRedirects(true);
            if (c.getResponseCode() != 200) return false;
            File part = new File(dir, "libjnidispatch.so.part");
            boolean found = false;
            try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(c.getInputStream()))) {
                ZipEntry e;
                while ((e = zin.getNextEntry()) != null) {
                    if (!e.getName().equals(entryName)) continue;
                    Files.copy(zin, part.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
            Files.move(part.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return target.length() > 0L;
        } catch (Throwable t) {
            return false;
        }
    }

    private static String androidAbi() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        return arch.contains("aarch64") || arch.contains("arm64") ? "arm64-v8a"
                : arch.contains("arm") ? "armeabi-v7a"
                : arch.contains("64") ? "x86_64" : "x86";
    }

    /** Version of the JNA jar that Minecraft uses, read from jar metadata (this does NOT start JNA). */
    private static String jnaVersion() {
        try (InputStream in = JnaPreLaunch.class.getClassLoader()
                .getResourceAsStream("META-INF/maven/net.java.dev.jna/jna/pom.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String v = props.getProperty("version");
                if (v != null && !v.isEmpty()) return v;
            }
        } catch (Throwable ignored) {
            // use the fallback below
        }
        return "5.14.0";
    }

    public static boolean isAndroidLike() {
        return System.getenv("ANDROID_ROOT") != null
                || System.getenv("POJAV_RENDERER") != null
                || new File("/system/build.prop").exists();
    }

    private static boolean isExternal(File f) {
        String p = f.getAbsolutePath();
        return p.startsWith("/storage/") || p.startsWith("/sdcard") || p.startsWith("/mnt/");
    }

    /** Finds the Android app package (for example net.kdt.pojavlaunch) from the paths the launcher gives Java. */
    private static String detectPackage() {
        List<String> sources = new ArrayList<>();
        for (String key : new String[]{"user.home", "user.dir", "java.io.tmpdir", "java.home"}) {
            sources.add(System.getProperty(key, ""));
        }
        for (String key : new String[]{"POJAV_NATIVEDIR", "TMPDIR", "HOME", "POJAV_HOME", "LD_LIBRARY_PATH"}) {
            String v = System.getenv(key);
            sources.add(v == null ? "" : v);
        }
        Pattern[] patterns = {
                Pattern.compile("/Android/data/([A-Za-z0-9_.]+)/"),
                Pattern.compile("/data/(?:user/\\d+|data)/([A-Za-z0-9_.]+)/"),
                Pattern.compile("/data/app/[^/]+/([A-Za-z0-9_.]+)-[^/]+/")
        };
        for (String src : sources) {
            for (Pattern p : patterns) {
                Matcher m = p.matcher(src);
                if (m.find()) return m.group(1);
            }
        }
        return null;
    }

    /**
     * A writable folder inside the app's PRIVATE storage (shared storage cannot load native code).
     * Used by both this class and ModelManager, so they always agree on the folder.
     */
    public static synchronized File privateNativesDir() {
        if (dirCache != null) return dirCache;
        List<File> candidates = new ArrayList<>();
        String pkg = detectPackage();
        if (pkg != null) {
            candidates.add(new File("/data/user/0/" + pkg + "/files/supersonic-natives"));
            candidates.add(new File("/data/data/" + pkg + "/files/supersonic-natives"));
            candidates.add(new File("/data/user/0/" + pkg + "/cache/supersonic-natives"));
        }
        String tmp = System.getProperty("java.io.tmpdir");
        if (tmp != null) candidates.add(new File(tmp, "supersonic-natives"));
        String envTmp = System.getenv("TMPDIR");
        if (envTmp != null) candidates.add(new File(envTmp, "supersonic-natives"));

        for (File c : candidates) {
            if (isExternal(c)) continue;
            if ((c.isDirectory() || c.mkdirs()) && c.canWrite()) {
                dirCache = c;
                return c;
            }
        }
        dirCache = new File(System.getProperty("java.io.tmpdir", "."), "supersonic-natives");
        return dirCache;
    }
}
