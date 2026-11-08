package net.supersonic.voice;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        } catch (Throwable ignored) {
            // must never stop the game from starting
        }
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
