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

    // Gameplay
    public boolean smartSprint = false;

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
                SupersonicConfig cfg = GSON.fromJson(Files.readString(FILE), SupersonicConfig.class);
                if (cfg != null) {
                    if (cfg.corner == null) cfg.corner = Corner.TOP_LEFT;
                    if (cfg.accent == null) cfg.accent = Accent.BLUE;
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
