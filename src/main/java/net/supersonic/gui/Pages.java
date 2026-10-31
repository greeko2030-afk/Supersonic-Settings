package net.supersonic.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Util;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;
import net.supersonic.gui.Rows.Row;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Builds the list of rows for every tab. Only real, working options are included. */
public final class Pages {
    private Pages() {
    }

    private static final Function<Double, String> PCT = v -> String.format(Locale.ROOT, "%.0f%%", v * 100.0);
    private static final Function<Double, String> INT = v -> String.format(Locale.ROOT, "%.0f", v);
    private static final Function<Double, String> CHUNKS = v -> String.format(Locale.ROOT, "%.0f Chunks", v);

    public static Map<Tab, List<Row>> build(MinecraftClient mc, SupersonicScreen screen) {
        Map<Tab, List<Row>> map = new EnumMap<>(Tab.class);
        map.put(Tab.GAMEPLAY, gameplay(mc));
        map.put(Tab.VISUALS, visuals(mc));
        map.put(Tab.PERFORMANCE, performance(mc, screen));
        map.put(Tab.HUD, hud());
        map.put(Tab.AUDIO, audio(mc));
        map.put(Tab.MULTIPLAYER, multiplayer(mc));
        map.put(Tab.CONTROLS, controls(mc, screen));
        map.put(Tab.MODS, mods(screen));
        map.put(Tab.CAPTURE, capture(mc));
        return map;
    }

    // ------------------------------------------------------------------ pages

    private static List<Row> gameplay(MinecraftClient mc) {
        GameOptions o = mc.options;
        SupersonicConfig c = SupersonicClient.CONFIG;
        List<Row> r = new ArrayList<>();
        r.add(new Rows.Header("GENERAL"));
        r.add(cycle("Attack Indicator", "Show attack cooldown indicator.", o.getAttackIndicator()));
        r.add(toggle("Auto Jump", "Automatically jump when moving forward.", o.getAutoJump()));
        r.add(toggle("Sprint Toggle", "Toggle sprint on key press.", o.getSprintToggled()));
        r.add(toggle("Sneak Toggle", "Toggle sneak on key press.", o.getSneakToggled()));
        r.add(new Rows.Toggle("Smart Sprint", "Automatically sprint while moving forward.",
                () -> c.smartSprint, v -> c.smartSprint = v));
        r.add(toggle("View Bobbing", "Camera bobbing while walking.", o.getBobView()));
        r.add(new Rows.Toggle("Pause on Lost Focus", "Pause the game when the window is unfocused.",
                () -> o.pauseOnLostFocus, v -> o.pauseOnLostFocus = v));
        r.add(new Rows.Header("CAMERA"));
        r.add(slider("Field of View", "Camera field of view.", o.getFov(), 30, 110, 1, INT));
        r.add(slider("FOV Effects", "How much speed changes the FOV.", o.getFovEffectScale(), 0, 1, 0.05, PCT));
        r.add(slider("Distortion Effects", "Nausea and portal wobble strength.", o.getDistortionEffectScale(), 0, 1, 0.05, PCT));
        r.add(new Rows.Header("INTERFACE"));
        r.add(toggle("Subtitles", "Show text for sounds.", o.getShowSubtitles()));
        r.add(new Rows.Toggle("Advanced Tooltips", "Show item ids and durability in tooltips.",
                () -> o.advancedItemTooltips, v -> o.advancedItemTooltips = v));
        return r;
    }

    private static List<Row> visuals(MinecraftClient mc) {
        GameOptions o = mc.options;
        List<Row> r = new ArrayList<>();
        r.add(new Rows.Header("GRAPHICS QUALITY"));
        r.add(cycle("Graphics Quality", "Overall graphics quality level.", o.getGraphicsMode()));
        r.add(toggle("Smooth Lighting", "Enable smooth lighting for better visuals.", o.getAo()));
        r.add(cycle("Cloud Quality", "Adjust cloud rendering.", o.getCloudRenderMode()));
        r.add(cycle("Particles", "How many particles are shown.", o.getParticles()));
        r.add(toggle("Entity Shadows", "Render shadows for entities.", o.getEntityShadows()));
        r.add(slider("Biome Blend", "Smoothness of biome color changes.", o.getBiomeBlendRadius(), 0, 7, 1, INT));
        r.add(slider("Mipmap Levels", "Texture smoothing at a distance.", o.getMipmapLevels(), 0, 4, 1, INT));
        r.add(new Rows.Header("ADVANCED"));
        r.add(slider("Entity Distance", "How far entities are rendered.", o.getEntityDistanceScaling(), 0.5, 5.0, 0.25, PCT));
        r.add(slider("Brightness", "Adjust screen brightness.", o.getGamma(), 0, 1, 0.01, PCT));
        return r;
    }

    private static List<Row> performance(MinecraftClient mc, SupersonicScreen screen) {
        GameOptions o = mc.options;
        List<Row> r = new ArrayList<>();
        r.add(new Rows.Header("FPS BOOST"));
        r.add(slider("Max FPS", "Set the maximum FPS limit.", o.getMaxFps(), 10, 260, 10,
                v -> v >= 260 ? "Unlimited" : String.format(Locale.ROOT, "%.0f FPS", v)));
        r.add(toggle("VSync", "Sync frames with your monitor.", o.getEnableVsync()));
        r.add(new Rows.Button("Optimize Now", "Apply safe vanilla performance tweaks.", "Optimize", () -> optimizeNow(mc)));
        r.add(new Rows.Header("RENDER OPTIMIZATION"));
        r.add(slider("Render Distance", "Adjust how far you can see.", o.getViewDistance(), 2, 32, 1, CHUNKS));
        r.add(slider("Simulation Distance", "How far entities and chunks are simulated.", o.getSimulationDistance(), 5, 32, 1, CHUNKS));
        r.add(cycle("Chunk Builder", "How chunk updates are processed.", o.getChunkBuilderMode()));
        r.add(new Rows.Header("SYSTEM"));
        r.add(new Rows.Info("Java Heap", "Memory used by the game.", () -> {
            Runtime rt = Runtime.getRuntime();
            return ((rt.totalMemory() - rt.freeMemory()) / 1048576L) + " / " + (rt.maxMemory() / 1048576L) + " MB";
        }, Draw.WHITE, null));
        r.add(new Rows.Info("CPU Threads", "Logical processors available.",
                () -> String.valueOf(Runtime.getRuntime().availableProcessors()), Draw.WHITE, null));
        r.add(new Rows.Info("Java Version", "Runtime the game is using.",
                () -> System.getProperty("java.version"), Draw.WHITE, null));
        return r;
    }

    private static List<Row> hud() {
        SupersonicConfig c = SupersonicClient.CONFIG;
        List<Row> r = new ArrayList<>();
        r.add(new Rows.Header("HUD ELEMENTS"));
        r.add(new Rows.Toggle("HUD Enabled", "Show the Supersonic HUD in game.", () -> c.hudEnabled, v -> c.hudEnabled = v));
        r.add(new Rows.Toggle("FPS Counter", "Show FPS in game.", () -> c.showFps, v -> c.showFps = v));
        r.add(new Rows.Toggle("CPS Counter", "Show clicks per second.", () -> c.showCps, v -> c.showCps = v));
        r.add(new Rows.Toggle("Coordinates", "Show your coordinates.", () -> c.showCoordinates, v -> c.showCoordinates = v));
        r.add(new Rows.Toggle("Direction", "Show facing direction.", () -> c.showDirection, v -> c.showDirection = v));
        r.add(new Rows.Toggle("Biome", "Show the current biome.", () -> c.showBiome, v -> c.showBiome = v));
        r.add(new Rows.Toggle("Clock", "Show in-game time.", () -> c.showClock, v -> c.showClock = v));
        r.add(new Rows.Toggle("RAM Usage", "Show memory usage.", () -> c.showMemory, v -> c.showMemory = v));
        r.add(new Rows.Toggle("Potion HUD", "Show active potion effects.", () -> c.showPotions, v -> c.showPotions = v));
        r.add(new Rows.Toggle("Armor HUD", "Show armor pieces and durability.", () -> c.showArmor, v -> c.showArmor = v));
        r.add(new Rows.Header("HUD APPEARANCE"));
        r.add(new Rows.Slider("HUD Scale", "Adjust the size of HUD elements.", 0.5, 2.0, 0.05,
                () -> (double) c.hudScale, v -> c.hudScale = v.floatValue(), PCT));
        r.add(new Rows.Toggle("HUD Background", "Show background for better visibility.",
                () -> c.showBackground, v -> c.showBackground = v));
        r.add(new Rows.Slider("Background Opacity", "How dark the HUD background is.", 0.0, 1.0, 0.05,
                () -> (double) c.bgOpacity, v -> c.bgOpacity = v.floatValue(), PCT));
        r.add(new Rows.Cycle("HUD Color Theme", "Choose your HUD accent color.",
                () -> c.accent, v -> c.accent = (SupersonicConfig.Accent) v,
                () -> SupersonicConfig.Accent.values(), v -> ((SupersonicConfig.Accent) v).label));
        r.add(new Rows.Header("HUD POSITION"));
        r.add(new Rows.Cycle("HUD Alignment", "Choose HUD alignment on screen.",
                () -> c.corner, v -> c.corner = (SupersonicConfig.Corner) v,
                () -> SupersonicConfig.Corner.values(), v -> ((SupersonicConfig.Corner) v).label));
        r.add(new Rows.Slider("X Offset", "Horizontal offset.", 0, 200, 1,
                () -> (double) c.offsetX, v -> c.offsetX = (int) Math.round(v), INT));
        r.add(new Rows.Slider("Y Offset", "Vertical offset.", 0, 200, 1,
                () -> (double) c.offsetY, v -> c.offsetY = (int) Math.round(v), INT));
        return r;
    }

    private static List<Row> audio(MinecraftClient mc) {
        GameOptions o = mc.options;
        List<Row> r = new ArrayList<>();
        r.add(new Rows.Header("VOLUME MIXER"));
        r.add(volume(o, SoundCategory.MASTER, "Master Volume", "Overall game volume."));
        r.add(volume(o, SoundCategory.MUSIC, "Music", "Background music volume."));
        r.add(volume(o, SoundCategory.RECORDS, "Jukebox / Note Blocks", "Music from jukebox and note blocks."));
        r.add(volume(o, SoundCategory.WEATHER, "Weather", "Rain, thunder and weather sounds."));
        r.add(volume(o, SoundCategory.BLOCKS, "Blocks", "Block sounds and environment."));
        r.add(volume(o, SoundCategory.HOSTILE, "Hostile Creatures", "Hostile mobs and monster sounds."));
        r.add(volume(o, SoundCategory.NEUTRAL, "Friendly Creatures", "Passive mobs and ambient creatures."));
        r.add(volume(o, SoundCategory.PLAYERS, "Players", "Other players' sounds."));
        r.add(volume(o, SoundCategory.AMBIENT, "Ambient / Environment", "Ambient sounds and atmosphere."));
        r.add(volume(o, SoundCategory.VOICE, "Voice / Speech", "Voice and speech sounds."));
        r.add(new Rows.Header("OPTIONS"));
        r.add(toggle("Subtitles", "Show text for sounds.", o.getShowSubtitles()));
        return r;
    }

    private static List<Row> multiplayer(MinecraftClient mc) {
        GameOptions o = mc.options;
        List<Row> r = new ArrayList<>();
        r.add(new Rows.Header("CHAT"));
        r.add(cycle("Chat Visibility", "Which chat messages you see.", o.getChatVisibility()));
        r.add(toggle("Chat Colors", "Show colors in chat messages.", o.getChatColors()));
        r.add(toggle("Chat Links", "Make links in chat clickable.", o.getChatLinks()));
        r.add(slider("Chat Opacity", "Opacity of chat text.", o.getChatOpacity(), 0, 1, 0.05, PCT));
        r.add(slider("Chat Background", "Opacity of the chat background.", o.getTextBackgroundOpacity(), 0, 1, 0.05, PCT));
        r.add(slider("Chat Scale", "Size of chat text.", o.getChatScale(), 0, 1, 0.05, PCT));
        r.add(slider("Chat Line Spacing", "Space between chat lines.", o.getChatLineSpacing(), 0, 1, 0.05, PCT));
        r.add(new Rows.Header("CONNECTION"));
        r.add(new Rows.Info("Current Server", "The server you are connected to.", () -> {
            ServerInfo s = mc.getCurrentServerEntry();
            if (s != null) return s.address;
            return mc.isInSingleplayer() ? "Singleplayer" : "Not connected";
        }, Draw.WHITE, null));
        r.add(new Rows.Info("Ping", "Latency to the server.", () -> {
            ServerInfo s = mc.getCurrentServerEntry();
            return s != null ? s.ping + " ms" : "-";
        }, Draw.GREEN, null));
        return r;
    }

    private static List<Row> controls(MinecraftClient mc, SupersonicScreen screen) {
        GameOptions o = mc.options;
        Runnable openKeys = () -> mc.setScreen(new KeybindsScreen(screen, mc.options));
        List<Row> r = new ArrayList<>();
        r.add(new Rows.Header("MOVEMENT"));
        r.add(key("Move Forward", o.forwardKey, openKeys));
        r.add(key("Move Backward", o.backKey, openKeys));
        r.add(key("Move Left", o.leftKey, openKeys));
        r.add(key("Move Right", o.rightKey, openKeys));
        r.add(key("Jump", o.jumpKey, openKeys));
        r.add(key("Sneak", o.sneakKey, openKeys));
        r.add(key("Sprint", o.sprintKey, openKeys));
        r.add(new Rows.Header("GAMEPLAY"));
        r.add(key("Attack / Destroy", o.attackKey, openKeys));
        r.add(key("Use Item / Place Block", o.useKey, openKeys));
        r.add(key("Pick Block", o.pickItemKey, openKeys));
        r.add(key("Drop Selected Item", o.dropKey, openKeys));
        r.add(key("Inventory", o.inventoryKey, openKeys));
        r.add(key("Swap Hands", o.swapHandsKey, openKeys));
        r.add(new Rows.Button("All Keybinds", "Click any key above, or open the full list.", "Open", openKeys));
        r.add(new Rows.Header("MOUSE"));
        r.add(slider("Mouse Sensitivity", "How fast the camera turns.", o.getMouseSensitivity(), 0, 1, 0.01,
                v -> String.format(Locale.ROOT, "%.0f%%", v * 200.0)));
        r.add(toggle("Invert Mouse", "Invert vertical mouse movement.", o.getInvertYMouse()));
        r.add(toggle("Discrete Scrolling", "Scroll one step at a time.", o.getDiscreteMouseScroll()));
        r.add(toggle("Raw Mouse Input", "Use raw mouse input.", o.getRawMouseInput()));
        return r;
    }

    private static List<Row> mods(SupersonicScreen screen) {
        List<Row> r = new ArrayList<>();
        r.add(new Rows.Header("LOADED MODS (" + screen.mods.size() + ")"));
        for (int i = 0; i < screen.mods.size(); i++) {
            final int idx = i;
            ModContainer m = screen.mods.get(i);
            String desc = m.getMetadata().getDescription();
            if (desc == null) desc = "";
            int nl = desc.indexOf('\n');
            if (nl >= 0) desc = desc.substring(0, nl);
            r.add(new Rows.ModRow(m.getMetadata().getName(), desc.trim(),
                    m.getMetadata().getVersion().getFriendlyString(),
                    () -> screen.selectedMod == idx, () -> screen.selectedMod = idx));
        }
        return r;
    }

    private static List<Row> capture(MinecraftClient mc) {
        List<Row> r = new ArrayList<>();
        r.add(new Rows.Header("FOLDERS"));
        r.add(folder("Screenshots", "Open the screenshots folder.", "screenshots"));
        r.add(folder("Worlds", "Open the saves folder.", "saves"));
        r.add(folder("Resource Packs", "Open the resource packs folder.", "resourcepacks"));
        r.add(folder("Shader Packs", "Open the shader packs folder.", "shaderpacks"));
        r.add(folder("Mods", "Open the mods folder.", "mods"));
        r.add(folder("Logs", "Open the logs folder.", "logs"));
        r.add(folder("Config", "Open the config folder.", "config"));
        r.add(new Rows.Header("REPLAY"));
        r.add(new Rows.Info("Replay Mod", "Detected automatically if installed.",
                () -> FabricLoader.getInstance().isModLoaded("replaymod") ? "Installed" : "Not installed",
                FabricLoader.getInstance().isModLoaded("replaymod") ? Draw.GREEN : Draw.GRAY, null));
        r.add(folder("Replays", "Open the replay recordings folder.", "replay_recordings"));
        return r;
    }

    // ---------------------------------------------------------------- helpers

    private static Row folder(String title, String desc, String dirName) {
        return new Rows.Button(title, desc, "Open", () -> {
            File f = FabricLoader.getInstance().getGameDir().resolve(dirName).toFile();
            f.mkdirs();
            Util.getOperatingSystem().open(f);
        });
    }

    private static Row key(String title, KeyBinding kb, Runnable open) {
        return new Rows.Info(title, "Click to change in the keybinds screen.",
                () -> kb.getBoundKeyLocalizedText().getString(), Draw.WHITE, open);
    }

    private static Row volume(GameOptions o, SoundCategory cat, String title, String desc) {
        return slider(title, desc, o.getSoundVolumeOption(cat), 0, 1, 0.05, PCT);
    }

    @SuppressWarnings("unchecked")
    private static Row toggle(String title, String desc, SimpleOption<?> opt) {
        return new Rows.Toggle(title, desc,
                () -> {
                    Object v = opt.getValue();
                    return v instanceof Boolean && (Boolean) v;
                },
                v -> setRaw(opt, v));
    }

    private static Row cycle(String title, String desc, SimpleOption<?> opt) {
        return new Rows.Cycle(title, desc,
                () -> opt.getValue(),
                v -> setRaw(opt, v),
                () -> ((Enum<?>) opt.getValue()).getDeclaringClass().getEnumConstants(),
                null);
    }

    private static Row slider(String title, String desc, SimpleOption<?> opt,
                              double min, double max, double step, Function<Double, String> fmt) {
        return new Rows.Slider(title, desc, min, max, step,
                () -> ((Number) opt.getValue()).doubleValue(),
                v -> setNumber(opt, v), fmt);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setRaw(SimpleOption<?> opt, Object value) {
        ((SimpleOption) opt).setValue(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setNumber(SimpleOption<?> opt, double v) {
        SimpleOption raw = (SimpleOption) opt;
        if (opt.getValue() instanceof Integer) {
            raw.setValue(Integer.valueOf((int) Math.round(v)));
        } else {
            raw.setValue(Double.valueOf(v));
        }
    }

    /** Real vanilla option changes that raise FPS. */
    private static void optimizeNow(MinecraftClient mc) {
        GameOptions o = mc.options;
        o.getViewDistance().setValue(10);
        o.getSimulationDistance().setValue(8);
        o.getMaxFps().setValue(260);
        o.getEnableVsync().setValue(false);
        o.getEntityShadows().setValue(false);
        o.getGraphicsMode().setValue(GraphicsMode.FAST);
        o.getCloudRenderMode().setValue(CloudRenderMode.FAST);
        o.getParticles().setValue(ParticlesMode.DECREASED);
        o.getBiomeBlendRadius().setValue(1);
    }
}
