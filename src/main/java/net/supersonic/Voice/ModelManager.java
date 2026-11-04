package net.supersonic.voice;

import com.sun.jna.Platform;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Downloads speech models and prepares the Vosk native library. Everything lives in config/supersonic/vosk. */
public final class ModelManager {
    /** Display name, official small model folder name. */
    private static final String[][] MODELS = {
            {"English (US)", "vosk-model-small-en-us-0.15"},
            {"English (India)", "vosk-model-small-en-in-0.4"},
            {"Hindi", "vosk-model-small-hi-0.22"},
            {"Spanish", "vosk-model-small-es-0.42"},
            {"French", "vosk-model-small-fr-0.22"},
            {"German", "vosk-model-small-de-0.15"},
            {"Russian", "vosk-model-small-ru-0.22"},
            {"Portuguese", "vosk-model-small-pt-0.3"},
            {"Turkish", "vosk-model-small-tr-0.3"},
            {"Italian", "vosk-model-small-it-0.22"},
            {"Japanese", "vosk-model-small-ja-0.22"},
            {"Korean", "vosk-model-small-ko-0.22"},
            {"Chinese", "vosk-model-small-cn-0.22"},
            {"Dutch", "vosk-model-small-nl-0.22"},
            {"Vietnamese", "vosk-model-small-vn-0.4"}
    };

    private static final String ANDROID_AAR =
            "https://repo1.maven.org/maven2/com/alphacephei/vosk-android/0.3.45/vosk-android-0.3.45.aar";

    private static volatile String dlStatus = "Not downloaded";
    private static volatile boolean downloading = false;
    private static boolean nativesReady = false;

    private ModelManager() {
    }

    public static File root() {
        return FabricLoader.getInstance().getConfigDir().resolve("supersonic").resolve("vosk").toFile();
    }

    public static String status() {
        return dlStatus;
    }

    public static boolean isDownloading() {
        return downloading;
    }

    private static String folderFor(String choice) {
        for (String[] m : MODELS) {
            if (m[0].equals(choice)) return m[1];
        }
        return choice;
    }

    public static File modelDir(String choice) {
        return new File(root(), folderFor(choice));
    }

    public static boolean installed(File dir) {
        return new File(dir, "am").isDirectory() || new File(dir, "conf").isDirectory();
    }

    public static boolean isInstalled(String choice) {
        return installed(modelDir(choice));
    }

    /** Known models plus any folder the user put into config/supersonic/vosk (for example a Bengali model). */
    public static Object[] choices() {
        List<Object> list = new ArrayList<>();
        for (String[] m : MODELS) list.add(m[0]);
        File[] dirs = root().listFiles(File::isDirectory);
        if (dirs != null) {
            for (File d : dirs) {
                boolean known = false;
                for (String[] m : MODELS) {
                    if (m[1].equals(d.getName())) {
                        known = true;
                        break;
                    }
                }
                if (!known && !d.getName().equals("natives") && installed(d)) list.add(d.getName());
            }
        }
        return list.toArray();
    }

    // ------------------------------------------------------------ download

    public static void downloadAsync(String choice) {
        if (downloading) return;
        downloading = true;
        dlStatus = "Starting...";
        Thread t = new Thread(() -> {
            try {
                if (VoiceManager.isAndroidLike()) {
                    dlStatus = "Getting the Android speech library...";
                    prepareNatives();
                }
                String folder = folderFor(choice);
                boolean known = false;
                for (String[] m : MODELS) {
                    if (m[1].equals(folder)) known = true;
                }
                if (!known) {
                    dlStatus = installed(new File(root(), folder)) ? "Custom model ready" : "Custom model folder is empty";
                    return;
                }
                File dir = new File(root(), folder);
                if (!installed(dir)) downloadModel(folder);
                dlStatus = "Ready";
            } catch (Throwable e) {
                String m = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                dlStatus = "Error: " + (m.length() > 80 ? m.substring(0, 80) : m);
            } finally {
                downloading = false;
            }
        }, "Supersonic-Voice-Download");
        t.setDaemon(true);
        t.start();
    }

    private static void downloadModel(String folder) throws IOException {
        File rootDir = root();
        rootDir.mkdirs();
        File part = new File(rootDir, folder + ".zip.part");
        HttpURLConnection c = (HttpURLConnection) URI.create("https://alphacephei.com/vosk/models/" + folder + ".zip")
                .toURL().openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.setInstanceFollowRedirects(true);
        int code = c.getResponseCode();
        if (code != 200) throw new IOException("HTTP " + code + " for " + folder);
        long total = c.getContentLengthLong();
        try (InputStream in = c.getInputStream(); OutputStream out = new FileOutputStream(part)) {
            byte[] buf = new byte[65536];
            long done = 0L;
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                done += n;
                dlStatus = total > 0 ? "Downloading " + (done * 100L / total) + "%" : "Downloading " + (done / 1048576L) + " MB";
            }
        }
        dlStatus = "Unpacking...";
        unzip(part, rootDir);
        part.delete();
    }

    private static void unzip(File zip, File destDir) throws IOException {
        Path base = destDir.toPath().toAbsolutePath().normalize();
        try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                Path out = base.resolve(e.getName()).normalize();
                if (!out.startsWith(base)) throw new IOException("Bad file path in zip");
                if (e.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    Files.copy(zin, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    // ------------------------------------------------------------- natives

    /** Makes the Vosk native library loadable. Safe to call many times. */
    public static synchronized void prepareNatives() throws IOException {
        if (nativesReady) return;
        File dir = new File(root(), "natives");
        dir.mkdirs();
        if (VoiceManager.isAndroidLike()) {
            androidNatives(dir);
        } else {
            bundledNatives(dir);
        }
        String old = System.getProperty("jna.library.path");
        String path = dir.getAbsolutePath();
        System.setProperty("jna.library.path", old == null || old.isEmpty() ? path : path + File.pathSeparator + old);
        nativesReady = true;
    }

    /** Desktop: copy the libraries that ship inside the Vosk jar next to each other. */
    private static void bundledNatives(File dir) throws IOException {
        String[] prefixes = {Platform.RESOURCE_PREFIX, "darwin"};
        String[] names = {"libvosk.dll", "libgcc_s_seh-1.dll", "libstdc++-6.dll", "libwinpthread-1.dll",
                "libvosk.so", "libvosk.dylib"};
        ClassLoader cl = ModelManager.class.getClassLoader();
        boolean any = false;
        for (String prefix : prefixes) {
            for (String name : names) {
                try (InputStream in = cl.getResourceAsStream(prefix + "/" + name)) {
                    if (in == null) continue;
                    Files.copy(in, new File(dir, name).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    any = true;
                }
            }
            if (any) break;
        }
        if (!any) throw new IOException("Vosk library for " + Platform.RESOURCE_PREFIX + " is not inside the mod");
        File dll = new File(dir, "libvosk.dll");
        if (dll.exists()) Files.copy(dll.toPath(), new File(dir, "vosk.dll").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /** Android: take libvosk.so for this phone's CPU from the official vosk-android package on Maven Central. */
    private static void androidNatives(File dir) throws IOException {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        String abi = arch.contains("aarch64") || arch.contains("arm64") ? "arm64-v8a"
                : arch.contains("arm") ? "armeabi-v7a"
                : arch.contains("64") ? "x86_64" : "x86";
        File lib = new File(dir, "libvosk.so");
        if (!lib.exists()) {
            HttpURLConnection c = (HttpURLConnection) URI.create(ANDROID_AAR).toURL().openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(30000);
            c.setInstanceFollowRedirects(true);
            if (c.getResponseCode() != 200) throw new IOException("HTTP " + c.getResponseCode() + " for vosk-android");
            String wanted = "jni/" + abi + "/";
            boolean any = false;
            try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(c.getInputStream()))) {
                ZipEntry e;
                while ((e = zin.getNextEntry()) != null) {
                    String n = e.getName();
                    if (e.isDirectory() || !n.startsWith(wanted) || !n.endsWith(".so")) continue;
                    String fileName = n.substring(wanted.length());
                    if (fileName.contains("/")) continue;
                    Files.copy(zin, new File(dir, fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    any = true;
                }
            }
            if (!any || !lib.exists()) throw new IOException("libvosk.so for " + abi + " not found in vosk-android");
        }
        File cxx = new File(dir, "libc++_shared.so");
        if (cxx.exists()) {
            try {
                System.load(cxx.getAbsolutePath());
            } catch (Throwable ignored) {
                // may already be loaded by the launcher
            }
        }
    }
}
