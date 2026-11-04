package net.supersonic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Mod settings, saved to config/supersonic-settings.json */
public class SupersonicConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("supersonic-settings.json");

    public enum Corner {
        TOP_LEFT("Top Left"),
        TOP_RIGHT("Top Right"),
        BOTTOM_LEFT("Bottom Left"),
        BOTTOM_RIGHT("Bottom Right");

        public final String label;

        Corner(String label) {
            this.label = label;
        }
    }

    public enum Accent {
        BLUE("Supersonic Blue", "§b", 0xFF00D4FF),
        GREEN("Emerald", "§a", 0xFF3DFF7A),
        RED("Crimson", "§c", 0xFFFF5555),
        PURPLE("Violet", "§d", 0xFFD06BFF),
        GOLD("Gold", "§6", 0xFFFFAA00),
        WHITE("Classic White", "§f", 0xFFFFFFFF);

        public final String label;
        public final String code;
        public final int argb;

        Accent(String label, String code, int argb) {
            this.label = label;
            this.code = code;
            this.argb = argb;
        }
    }

    public enum VoiceEngine {
        AUTO("Auto"),
        WINDOWS("Windows (built-in)"),
        VOSK("Offline Model (Vosk)");

        public final String label;

        VoiceEngine(String label) {
            this.label = label;
        }
    }

    // Gameplay
    public boolean smartSprint = false;

    // Voice chat (offline speech-to-text, Windows)
    public boolean voiceEnabled = true;
    public String voiceLang = "";
    public float voiceSensitivity = 0.7f;
    public int voiceSilenceMs = 700;
    public float voiceMinConfidence = 0.30f;
    public boolean voiceAutoCommands = true;
    public String voiceCommandWord = "command";
    public boolean voiceAnimation = true;
    public VoiceEngine voiceEngine = VoiceEngine.AUTO;
    public String voiceVoskModel = "English (US)";

    // Capture
    public boolean screenshotHideHud = true;

    // HUD elements
    public boolean hudEnabled = true;
    public boolean showFps = true;
    public boolean showCps = true;
    public boolean showCoordinates = true;
    public boolean showDirection = true;
    public boolean showBiome = false;
    public boolean showClock = true;
    public boolean showMemory = false;
    public boolean showArmor = false;
    public boolean showPotions = true;

    // HUD appearance / position
    public boolean showBackground = true;
    public float bgOpacity = 0.56f;
    public float hudScale = 1.0f;
    public int offsetX = 6;
    public int offsetY = 6;
    public Corner corner = Corner.TOP_LEFT;
    public Accent accent = Accent.BLUE;

    public static SupersonicConfig load() {
        try {
            if (Files.exists(FILE)) {
                String raw = Files.readString(FILE);
                SupersonicConfig cfg = GSON.fromJson(raw, SupersonicConfig.class);
                if (cfg != null) {
                    if (cfg.voiceEngine == null) cfg.voiceEngine = VoiceEngine.AUTO;
                    if (cfg.voiceVoskModel == null || cfg.voiceVoskModel.isBlank()) cfg.voiceVoskModel = "English (US)";
                    if (cfg.corner == null) cfg.corner = Corner.TOP_LEFT;
                    if (cfg.accent == null) cfg.accent = Accent.BLUE;
                    if (cfg.voiceLang == null) cfg.voiceLang = "";
                    if (cfg.voiceCommandWord == null) cfg.voiceCommandWord = "command";
                    if (cfg.voiceSensitivity < 0f || cfg.voiceSensitivity > 1f) cfg.voiceSensitivity = 0.7f;
                    if (cfg.voiceSilenceMs < 300 || cfg.voiceSilenceMs > 3000) cfg.voiceSilenceMs = 700;
                    if (cfg.voiceMinConfidence < 0f || cfg.voiceMinConfidence > 1f) cfg.voiceMinConfidence = 0.30f;
                    if (cfg.hudScale < 0.5f || cfg.hudScale > 2.0f) cfg.hudScale = 1.0f;
                    if (cfg.bgOpacity < 0f || cfg.bgOpacity > 1f) cfg.bgOpacity = 0.56f;
                    return cfg;
                }
            }
        } catch (Exception e) {
            SupersonicClient.LOGGER.warn("Could not read config, using defaults", e);
        }
        SupersonicConfig cfg = new SupersonicConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.writeString(FILE, GSON.toJson(this));
        } catch (Exception e) {
            SupersonicClient.LOGGER.warn("Could not save config", e);
        }
    }
}
