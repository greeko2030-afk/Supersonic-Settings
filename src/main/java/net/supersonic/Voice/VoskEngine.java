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

/** Offline speech recognition with Vosk. Runs on its own thread, fed by MicCapture. */
final class VoskEngine {
    private VoskEngine() {
    }

    static void run(int my) {
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
            VoiceManager.status = "Loading speech engine...";
            ModelManager.prepareNatives();
            LibVosk.setLogLevel(LogLevel.WARNINGS);

            try (Model model = new Model(dir.getAbsolutePath());
                 Recognizer rec = new Recognizer(model, 16000f)) {
                mic = MicCapture.open();
                VoiceManager.status = "Listening";
                VoiceManager.notifyPlayer("Microphone ON (" + cfg.voiceVoskModel + ", mic via " + mic.sourceName()
                        + "). Speak now. Left Shift + V to turn off.");

                byte[] buf = new byte[640]; // 20 ms
                long lastLoud = 0L;
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
            }
        } catch (Throwable t) {
            failed = true;
            VoiceManager.status = "Speech error";
            String m = t.getMessage() == null ? t.getClass().getSimpleName() : t.getClass().getSimpleName() + ": " + t.getMessage();
            VoiceManager.notifyPlayer("Offline speech failed: " + (m.length() > 150 ? m.substring(0, 150) : m));
            SupersonicClient.LOGGER.warn("Vosk engine failed", t);
        } finally {
            if (mic != null) mic.close();
            VoiceManager.finish(my, failed);
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
