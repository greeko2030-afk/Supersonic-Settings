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

    public boolean hudEnabled = true;
    public boolean showFps = true;
    public boolean showCps = true;
    public boolean showCoordinates = true;
    public boolean showDirection = true;
    public boolean showClock = true;
    public boolean showMemory = false;
    public boolean showBackground = true;
    public float hudScale = 1.0f;
    public int offsetX = 6;
    public int offsetY = 6;
    public Corner corner = Corner.TOP_LEFT;
    public boolean f3Info = true;

    public static SupersonicConfig load() {
        try {
            if (Files.exists(FILE)) {
                SupersonicConfig cfg = GSON.fromJson(Files.readString(FILE), SupersonicConfig.class);
                if (cfg != null) {
                    if (cfg.corner == null) cfg.corner = Corner.TOP_LEFT;
                    if (cfg.hudScale < 0.5f || cfg.hudScale > 2.0f) cfg.hudScale = 1.0f;
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

    /** Restore the HUD options to their defaults. */
    public void resetHud() {
        SupersonicConfig d = new SupersonicConfig();
        hudEnabled = d.hudEnabled;
        showFps = d.showFps;
        showCps = d.showCps;
        showCoordinates = d.showCoordinates;
        showDirection = d.showDirection;
        showClock = d.showClock;
        showMemory = d.showMemory;
        showBackground = d.showBackground;
        hudScale = d.hudScale;
        offsetX = d.offsetX;
        offsetY = d.offsetY;
        corner = d.corner;
        f3Info = d.f3Info;
    }
}
