package net.supersonic.voice;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Offline speech-to-text using the speech recognizer that is built into Windows
 * (System.Speech, driven through a hidden PowerShell process). No internet, no API key.
 * A second thread listens to the microphone only to drive the animation.
 */
public final class VoiceManager {
    private static final AudioFormat FORMAT = new AudioFormat(16000f, 16, 1, true, false);

    private static volatile boolean running = false;
    private static volatile boolean speaking = false;
    private static volatile float level = 0f;
    private static volatile String status = "Mic off";
    private static volatile String lastHeard = "";
    private static volatile long lastHeardAt = 0L;
    private static volatile long processingUntil = 0L;
    private static volatile int session = 0;
    private static volatile Process process = null;
    private static volatile String[] languages = {"Auto"};
    private static boolean hookAdded = false;

    private VoiceManager() {
    }

    // ------------------------------------------------------------- state

    public static boolean isOn() {
        return running;
    }

    public static boolean isSpeaking() {
        return speaking;
    }

    public static boolean isProcessing() {
        return running && !speaking && System.currentTimeMillis() < processingUntil;
    }

    public static float level() {
        return level;
    }

    public static String status() {
        return status;
    }

    public static String lastHeard() {
        return lastHeard;
    }

    public static long lastHeardAt() {
        return lastHeardAt;
    }

    static void setHeard(String text) {
        lastHeard = text;
        lastHeardAt = System.currentTimeMillis();
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    public static String[] languages() {
        return languages;
    }

    // ----------------------------------------------------------- control

    public static synchronized void toggle() {
        if (running) stop();
        else start();
    }

    public static synchronized void start() {
        if (running) return;
        if (!isWindows()) {
            status = "Windows only";
            notifyPlayer("Offline speech recognition works on Windows only (for now).");
            return;
        }
        if (!hookAdded) {
            hookAdded = true;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Process p = process;
                if (p != null) p.destroyForcibly();
            }));
        }
        final int my = ++session;
        running = true;
        status = "Starting...";

        Thread rec = new Thread(() -> recognizerLoop(my), "Supersonic-Voice-Recognizer");
        rec.setDaemon(true);
        rec.start();

        Thread lvl = new Thread(() -> levelLoop(my), "Supersonic-Voice-Level");
        lvl.setDaemon(true);
        lvl.start();
    }

    public static synchronized void stop() {
        if (!running) return;
        running = false;
        session++;
        Process p = process;
        if (p != null) p.destroy();
        speaking = false;
        level = 0f;
        status = "Mic off";
        notifyPlayer("Microphone OFF.");
    }

    // -------------------------------------------------- Windows recognizer

    private static String script(SupersonicConfig cfg) {
        String lang = cfg.voiceLang == null ? "" : cfg.voiceLang.replaceAll("[^A-Za-z-]", "");
        int silence = Math.max(300, Math.min(3000, cfg.voiceSilenceMs));
        String conf = String.format(Locale.ROOT, "%.2f", cfg.voiceMinConfidence);
        String s = """
                $ProgressPreference = 'SilentlyContinue'
                [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
                try {
                  Add-Type -AssemblyName System.Speech
                  $want = '__LANG__'
                  $all = [System.Speech.Recognition.SpeechRecognitionEngine]::InstalledRecognizers()
                  if ($all.Count -eq 0) { Write-Output 'ERR:No speech recognizer is installed in Windows (Settings > Time & language > Speech).'; exit 2 }
                  $rec = $null
                  if ($want -ne '') { foreach ($r in $all) { if ($r.Culture.Name -eq $want) { $rec = $r; break } } }
                  if ($rec -eq $null) {
                    $ui = [System.Globalization.CultureInfo]::CurrentUICulture.Name
                    foreach ($r in $all) { if ($r.Culture.Name -eq $ui) { $rec = $r; break } }
                  }
                  if ($rec -eq $null) { $rec = $all[0] }
                  $engine = New-Object System.Speech.Recognition.SpeechRecognitionEngine($rec.Culture)
                  $engine.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar))
                  $engine.SetInputToDefaultAudioDevice()
                  $engine.EndSilenceTimeout = [TimeSpan]::FromMilliseconds(__SIL__)
                  $engine.EndSilenceTimeoutAmbiguous = [TimeSpan]::FromMilliseconds(__SIL__)
                  Write-Output ('READY:' + $rec.Culture.Name)
                  while ($true) {
                    $res = $engine.Recognize([TimeSpan]::FromSeconds(60))
                    if ($res -ne $null -and $res.Text -ne '' -and $res.Confidence -ge __CONF__) { Write-Output ('T:' + $res.Text) }
                  }
                } catch {
                  Write-Output ('ERR:' + $_.Exception.Message)
                  exit 1
                }
                """;
        return s.replace("__LANG__", lang)
                .replace("__SIL__", String.valueOf(silence))
                .replace("__CONF__", conf);
    }

    private static ProcessBuilder powershell(String script) {
        String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoLogo", "-NoProfile", "-NonInteractive",
                "-ExecutionPolicy", "Bypass", "-EncodedCommand", encoded);
        pb.redirectErrorStream(true);
        return pb;
    }

    private static void recognizerLoop(int my) {
        Process p = null;
        try {
            p = powershell(script(SupersonicClient.CONFIG)).start();
            process = p;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (my != session || !running) break;
                    line = line.replace("\uFEFF", "").trim();
                    if (line.startsWith("READY")) {
                        status = "Listening";
                        String culture = line.length() > 6 ? line.substring(6) : "";
                        notifyPlayer("Microphone ON" + (culture.isEmpty() ? "" : " (" + culture + ")")
                                + ". Speak now. Left Shift + V to turn off.");
                    } else if (line.startsWith("T:")) {
                        final String text = line.substring(2).trim();
                        processingUntil = 0L;
                        if (!text.isEmpty()) {
                            MinecraftClient.getInstance().execute(() -> VoiceCommands.handle(text));
                        }
                    } else if (line.startsWith("ERR:")) {
                        status = "Mic error";
                        notifyPlayer("Speech engine: " + line.substring(4)
                                + " (Check Windows Settings > Privacy > Microphone.)");
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            status = "Mic error";
            notifyPlayer("Could not start speech recognition: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
        } finally {
            if (p != null) p.destroy();
            if (my == session) {
                running = false;
                speaking = false;
                level = 0f;
                if (!"Mic error".equals(status)) status = "Mic off";
            }
        }
    }

    // ---------------------------------------------------- animation level

    private static void levelLoop(int my) {
        TargetDataLine line = null;
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(FORMAT);
            line.start();

            byte[] buf = new byte[640]; // 20 ms
            long lastLoud = 0L;
            while (running && my == session) {
                int n = line.read(buf, 0, buf.length);
                if (n <= 0) continue;
                double r = rms(buf, n);
                level = level * 0.6f + (float) Math.min(1.0, r * 8.0) * 0.4f;

                double threshold = 0.05 - 0.045 * SupersonicClient.CONFIG.voiceSensitivity;
                long now = System.currentTimeMillis();
                if (r > threshold) {
                    speaking = true;
                    lastLoud = now;
                } else if (speaking && now - lastLoud > 350L) {
                    speaking = false;
                    processingUntil = now + 1500L;
                }
            }
        } catch (Throwable ignored) {
            // The animation just stays calm if Java cannot open the microphone.
        } finally {
            if (line != null) {
                try {
                    line.stop();
                    line.close();
                } catch (Throwable ignored) {
                    // already closed
                }
            }
            if (my == session) {
                speaking = false;
                level = 0f;
            }
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

    // ------------------------------------------------------------ languages

    /** Looks up the speech languages installed in Windows (runs once, in the background). */
    public static void prefetchLanguages() {
        if (!isWindows()) return;
        Thread t = new Thread(() -> {
            try {
                String ps = """
                        Add-Type -AssemblyName System.Speech
                        [System.Speech.Recognition.SpeechRecognitionEngine]::InstalledRecognizers() | ForEach-Object { Write-Output ('L:' + $_.Culture.Name) }
                        """;
                Process p = powershell(ps).start();
                List<String> found = new ArrayList<>();
                found.add("Auto");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.replace("\uFEFF", "").trim();
                        if (line.startsWith("L:") && line.length() > 2 && !found.contains(line.substring(2))) {
                            found.add(line.substring(2));
                        }
                    }
                }
                p.waitFor(5, TimeUnit.SECONDS);
                languages = found.toArray(new String[0]);
            } catch (Throwable ignored) {
                // keep the default list
            }
        }, "Supersonic-Voice-Languages");
        t.setDaemon(true);
        t.start();
    }

    public static void notifyPlayer(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§b[Supersonic] §f" + msg), false);
            }
        });
    }
}
