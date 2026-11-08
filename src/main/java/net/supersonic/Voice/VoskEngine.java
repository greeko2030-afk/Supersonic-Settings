package net.supersonic.voice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;

/** Offline speech recognition with Vosk. Runs on its own thread, fed by MicCapture. */
final class VoskEngine {
    private static boolean nativeReady = false;
    /** The first native-library failure. A failed LibVosk can never be loaded again in the same game session. */
    private static String initFailure = null;

    private VoskEngine() {
    }

    private static final Object RUN_LOCK = new Object();

    static void run(int my) {
        synchronized (RUN_LOCK) { // a new session waits until the previous one has released everything
            if (my != VoiceManager.session || !VoiceManager.running) return; // cancelled while waiting
            runLocked(my);
        }
    }

    private static void runLocked(int my) {
        SupersonicConfig cfg = SupersonicClient.CONFIG;
        File dir = ModelManager.modelDir(cfg.voiceVoskModel);
        boolean failed = false;
        MicCapture mic = null;
        try {
            if (!ModelManager.installed(dir)) {
                failed = true;
                VoiceManager.status = "Model missing";
                VoiceManager.notifyPlayer("Speech model \"" + cfg.voiceVoskModel
                        + "\" is not installed. Open Supersonic Settings > Voice Chat > Download Speech Files.");
                return;
            }

            VoiceManager.status = "Preparing speech library...";
            String err = initNative();
            if (err != null) {
                failed = true;
                VoiceManager.status = "Speech library error";
                VoiceManager.notifyPlayer("Speech library failed: " + err);
                VoiceManager.notifyPlayer("Details saved in config/supersonic/vosk-error.log");
                return;
            }

            VoiceManager.status = "Loading speech model...";
            try (Model model = new Model(dir.getAbsolutePath());
                 Recognizer rec = new Recognizer(model, 16000f)) {
                mic = MicCapture.open();
                final MicCapture micRef = mic;
                VoiceManager.status = "Listening";
                VoiceManager.notifyPlayer("Microphone ON (" + cfg.voiceVoskModel + ", mic via " + mic.sourceName()
                        + "). Speak now. Left Shift + V to turn off.");

                byte[] buf = new byte[640]; // 20 ms
                long lastLoud = 0L;
                try {
                while (VoiceManager.running && my == VoiceManager.session) {
                    int n = mic.read(buf);
                    if (n <= 0) continue;

                    // Level + "speaking" state for the animation
                    double r = rms(buf, n);
                    VoiceManager.level = VoiceManager.level * 0.6f + (float) Math.min(1.0, r * 8.0) * 0.4f;
                    double threshold = 0.05 - 0.045 * SupersonicClient.CONFIG.voiceSensitivity;
                    long now = System.currentTimeMillis();
                    if (r > threshold) {
                        VoiceManager.speaking = true;
                        lastLoud = now;
                    } else if (VoiceManager.speaking && now - lastLoud > 350L) {
                        VoiceManager.speaking = false;
                        VoiceManager.processingUntil = now + 1200L;
                    }

                    if (rec.acceptWaveForm(buf, n)) {
                        deliver(extract(rec.getResult()));
                    }
                }
                } finally {
                    micRef.close(); // release the microphone first
                    mic = null;
                }
            }
        } catch (Throwable t) {
            failed = true;
            VoiceManager.status = "Speech error";
            writeLog(t);
            VoiceManager.notifyPlayer("Offline speech failed: " + describe(t));
            VoiceManager.notifyPlayer("Details saved in config/supersonic/vosk-error.log");
            SupersonicClient.LOGGER.warn("Vosk engine failed", t);
        } finally {
            if (mic != null) mic.close();
            VoiceManager.finish(my, failed);
        }
    }

    /** Loads the native library once. Returns null on success, otherwise the reason. */
    private static synchronized String initNative() {
        if (nativeReady) return null;
        if (initFailure != null) return initFailure + " (restart the game after fixing this)";
        try {
            ModelManager.prepareNatives();
            LibVosk.setLogLevel(LogLevel.WARNINGS);
            nativeReady = true;
            return null;
        } catch (Throwable t) {
            String note = ModelManager.jnaNote();
            initFailure = describe(t) + (note.isEmpty() ? "" : " | " + note);
            if (initFailure.length() > 300) initFailure = initFailure.substring(0, 300);
            writeLog(t);
            SupersonicClient.LOGGER.warn("Vosk native library failed", t);
            return initFailure;
        }
    }

    /** "SomeError: message" of the deepest cause, short enough for the chat. */
    private static String describe(Throwable t) {
        Throwable root = t;
        int guard = 0;
        while (root.getCause() != null && root.getCause() != root && guard++ < 10) root = root.getCause();
        String m = root.getMessage() == null ? "" : ": " + root.getMessage().replace('\n', ' ');
        String s = root.getClass().getSimpleName() + m;
        return s.length() > 170 ? s.substring(0, 170) : s;
    }

    private static void writeLog(Throwable t) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("Supersonic voice (Vosk) error");
            pw.println("os.name=" + System.getProperty("os.name") + "  os.arch=" + System.getProperty("os.arch"));
            pw.println("java.version=" + System.getProperty("java.version") + "  vm=" + System.getProperty("java.vm.name"));
            pw.println("android-like=" + VoiceManager.isAndroidLike());
            pw.println("natives dir=" + ModelManager.nativesDir());
            pw.println("jna note=" + ModelManager.jnaNote());
            pw.println("jna.tmpdir=" + System.getProperty("jna.tmpdir"));
            pw.println("user.home=" + System.getProperty("user.home") + "  java.io.tmpdir=" + System.getProperty("java.io.tmpdir"));
            pw.println("jna.library.path=" + System.getProperty("jna.library.path"));
            pw.println("jna.boot.library.path=" + System.getProperty("jna.boot.library.path"));
            File nd = ModelManager.nativesDir();
            File[] list = nd.listFiles();
            if (list != null) {
                for (File f : list) pw.println("  " + f.getName() + " (" + f.length() + " bytes)");
            }
            pw.println();
            t.printStackTrace(pw);
            pw.flush();
            File log = ModelManager.root().getParentFile().toPath().resolve("vosk-error.log").toFile();
            log.getParentFile().mkdirs();
            Files.writeString(log.toPath(), sw.toString());
        } catch (Throwable ignored) {
            // logging must never crash the game
        }
    }

    private static void deliver(String text) {
        if (text.isEmpty()) return;
        VoiceManager.processingUntil = 0L;
        MinecraftClient.getInstance().execute(() -> VoiceCommands.handle(text));
    }

    private static String extract(String json) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            return o.has("text") ? o.get("text").getAsString().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static double rms(byte[] b, int n) {
        int samples = n / 2;
        if (samples == 0) return 0.0;
        long sum = 0L;
        for (int i = 0; i < samples; i++) {
            int s = (short) ((b[2 * i + 1] << 8) | (b[2 * i] & 0xFF));
            sum += (long) s * s;
        }
        return Math.sqrt(sum / (double) samples) / 32768.0;
    }
}
