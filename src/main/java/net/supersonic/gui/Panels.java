package net.supersonic.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;
import net.supersonic.hud.FrameStats;
import net.supersonic.hud.HudRenderer;
import net.supersonic.voice.VoiceManager;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Right-hand info panels. All numbers shown here are measured / read from the real game. */
public final class Panels {
    private Panels() {
    }

    private static final String[][] SOUNDS = {
            {"Rain & Thunder", "entity.lightning_bolt.thunder"},
            {"Cave Atmosphere", "ambient.cave"},
            {"Nether Ambient", "ambient.nether_wastes.mood"},
            {"Battle / Combat", "entity.ender_dragon.growl"},
            {"Note Block", "block.note_block.harp"},
            {"Level Up", "entity.player.levelup"}
    };

    private static final String[][] PERF_MODS = {
            {"Sodium", "sodium"}, {"Lithium", "lithium"}, {"Iris Shaders", "iris"},
            {"Indium", "indium"}, {"FerriteCore", "ferritecore"}, {"EntityCulling", "entityculling"},
            {"ImmediatelyFast", "immediatelyfast"}, {"ModernFix", "modernfix"}
    };

    public static void render(SupersonicScreen s, DrawContext ctx, TextRenderer tr, Tab tab,
                              int x, int y, int w, int h, int mx, int my) {
        switch (tab) {
            case PERFORMANCE -> performance(s, ctx, tr, x, y, w, h);
            case HUD -> hud(s, ctx, tr, x, y, w, h);
            case AUDIO -> audio(s, ctx, tr, x, y, w, h, mx, my);
            case MULTIPLAYER -> multiplayer(s, ctx, tr, x, y, w, h, mx, my);
            case VOICE -> voice(s, ctx, tr, x, y, w, h, mx, my);
            case CAPTURE -> capture(s, ctx, tr, x, y, w, h, mx, my);
            case CONTROLS -> controls(s, ctx, tr, x, y, w, h, mx, my);
            case MODS -> mods(s, ctx, tr, x, y, w, h, mx, my);
            default -> engine(ctx, tr, x, y, w, h);
        }
    }

    // ------------------------------------------------------------ shared bits

    private static void card(DrawContext ctx, int x, int y, int w, int h) {
        Draw.round(ctx, x, y, w, h, Draw.PANEL);
        Draw.outline(ctx, x, y, w, h, 0xAA0B7FA8);
    }

    private static void stat(DrawContext ctx, TextRenderer tr, int x, int y, int w, String label, String value, int color) {
        Draw.scaled(ctx, tr, label, x, y, Draw.GRAY, 0.75f);
        Draw.text(ctx, tr, tr.trimToWidth(value, w), x, y + 9, color);
    }

    private static String glString(int name) {
        try {
            String v = GL11.glGetString(name);
            return v == null ? "unknown" : v.trim();
        } catch (Throwable t) {
            return "unknown";
        }
    }

    private static String renderer() {
        FabricLoader l = FabricLoader.getInstance();
        if (l.isModLoaded("sodium")) {
            return "Sodium " + l.getModContainer("sodium").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
        }
        return "Vanilla";
    }

    private static String status(double fps) {
        if (fps >= 120) return "Optimal";
        if (fps >= 60) return "Good";
        if (fps >= 30) return "Fair";
        return "Low";
    }

    private static int statusColor(double fps) {
        if (fps >= 120) return Draw.GREEN;
        if (fps >= 60) return 0xFF9CFF3D;
        if (fps >= 30) return 0xFFFFD23D;
        return Draw.RED;
    }

    // ----------------------------------------------------------------- panels

    public static void engine(DrawContext ctx, TextRenderer tr, int x, int y, int w, int h) {
        card(ctx, x, y, w, h);
        Draw.centered(ctx, tr, "SUPERSONIC ENGINE", x + w / 2, y + 8, Draw.WHITE);
        Draw.ring(ctx, x + w / 2, y + 66, 34, 8, 0x8800D4FF);
        Draw.cube(ctx, x + w / 2, y + 22, 18);

        double fps = FrameStats.avgFps();
        int ly = y + 84;
        int tw = w - 20;
        String[][] items = {
                {"RENDERER", renderer()},
                {"GRAPHICS API", "OpenGL " + glString(GL11.GL_VERSION).split(" ")[0]},
                {"GPU", glString(GL11.GL_RENDERER)},
        };
        for (String[] it : items) {
            if (ly + 22 > y + h) return;
            stat(ctx, tr, x + 10, ly, tw, it[0], it[1], Draw.CYAN);
            ly += 24;
        }
        if (ly + 26 <= y + h) {
            Draw.scaled(ctx, tr, "FPS (AVERAGE)", x + 10, ly, Draw.GRAY, 0.75f);
            Draw.scaledShadow(ctx, tr, String.format(Locale.ROOT, "%.0f FPS", fps), x + 10, ly + 9, Draw.CYAN, 1.5f);
            ly += 30;
        }
        if (ly + 22 <= y + h) {
            stat(ctx, tr, x + 10, ly, tw, "ENGINE STATUS", status(fps), statusColor(fps));
        }
    }

    private static void performance(SupersonicScreen s, DrawContext ctx, TextRenderer tr, int x, int y, int w, int h) {
        card(ctx, x, y, w, h);
        Draw.text(ctx, tr, "PERFORMANCE OVERVIEW", x + 8, y + 8, Draw.CYAN);

        double fps = FrameStats.avgFps();
        Draw.ring(ctx, x + w / 2, y + 48, 30, 24, 0xFF0B7FA8);
        Draw.centered(ctx, tr, String.format(Locale.ROOT, "%.0f", fps), x + w / 2, y + 38, Draw.WHITE);
        Draw.centered(ctx, tr, "FPS", x + w / 2, y + 49, Draw.GRAY);
        Draw.centered(ctx, tr, status(fps), x + w / 2, y + 80, statusColor(fps));

        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        String[][] rows = {
                {"AVG FPS", String.format(Locale.ROOT, "%.0f", fps)},
                {"1% LOW", String.format(Locale.ROOT, "%.0f", FrameStats.lowFps())},
                {"FRAME TIME", String.format(Locale.ROOT, "%.2f ms", FrameStats.frameMs())},
                {"RAM USAGE", used + " / " + max + " MB"},
                {"CPU THREADS", String.valueOf(rt.availableProcessors())}
        };
        int ly = y + 96;
        for (String[] r : rows) {
            if (ly + 10 > y + h) return;
            Draw.scaled(ctx, tr, r[0], x + 8, ly + 1, Draw.GRAY, 0.8f);
            String v = r[1];
            Draw.text(ctx, tr, v, x + w - 8 - tr.getWidth(v), ly, Draw.WHITE);
            ly += 12;
        }

        ly += 6;
        if (ly + 10 > y + h) return;
        Draw.text(ctx, tr, "PERFORMANCE MODS", x + 8, ly, Draw.CYAN);
        ly += 13;
        FabricLoader l = FabricLoader.getInstance();
        for (String[] m : PERF_MODS) {
            if (ly + 9 > y + h) return;
            boolean on = l.isModLoaded(m[1]);
            Draw.scaled(ctx, tr, m[0], x + 8, ly, Draw.GRAY, 0.85f);
            String st = on ? "ACTIVE" : "NOT INSTALLED";
            Draw.scaled(ctx, tr, st, x + w - 8 - (int) (tr.getWidth(st) * 0.75f), ly, on ? Draw.GREEN : 0xFF6B7A8A, 0.75f);
            ly += 11;
        }
    }

    private static void hud(SupersonicScreen s, DrawContext ctx, TextRenderer tr, int x, int y, int w, int h) {
        MinecraftClient mc = MinecraftClient.getInstance();
        SupersonicConfig cfg = SupersonicClient.CONFIG;
        card(ctx, x, y, w, h);
        Draw.text(ctx, tr, "HUD PREVIEW", x + 8, y + 8, Draw.CYAN);

        int px = x + 8;
        int py = y + 22;
        int pw = w - 16;
        int ph = Math.min(120, h - 80);
        if (ph < 40) return;
        ctx.fill(px, py, px + pw, py + ph, 0xFF2E5B8C);
        ctx.fill(px, py + ph * 2 / 3, px + pw, py + ph, 0xFF2F6B2F);
        Draw.outline(ctx, px, py, pw, ph, Draw.CYAN_DIM);

        ctx.enableScissor(px, py, px + pw, py + ph);
        List<String> lines = HudRenderer.buildLines(mc, cfg);
        float sc = Math.min(1.0f, cfg.hudScale);
        HudRenderer.drawBox(ctx, mc, cfg, px + 4, py + 4, sc, lines);
        ctx.disableScissor();

        int ly = py + ph + 12;
        Draw.text(ctx, tr, "HUD INFORMATION", x + 8, ly, Draw.CYAN);
        ly += 13;
        String[] info = {
                "Open menu: Ctrl + R-Shift",
                "Alignment: " + cfg.corner.label,
                "Scale: " + Math.round(cfg.hudScale * 100) + "%",
                "Hidden while F3 is open"
        };
        for (String line : info) {
            if (ly + 9 > y + h) return;
            Draw.scaled(ctx, tr, line, x + 8, ly, Draw.GRAY, 0.85f);
            ly += 11;
        }
    }

    private static void audio(SupersonicScreen s, DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, int mx, int my) {
        MinecraftClient mc = MinecraftClient.getInstance();
        card(ctx, x, y, w, h);
        Draw.text(ctx, tr, "SOUND PREVIEW", x + 8, y + 8, Draw.CYAN);
        int ly = y + 24;
        for (String[] snd : SOUNDS) {
            if (ly + 22 > y + h) return;
            Draw.round(ctx, x + 6, ly, w - 12, 20, Draw.ROW);
            Draw.text(ctx, tr, tr.trimToWidth(snd[0], w - 44), x + 12, ly + 6, Draw.WHITE);
            final String id = snd[1];
            s.drawButton(ctx, x + w - 26, ly + 2, 18, 16, "", false, mx, my, () ->
                    mc.getSoundManager().play(PositionedSoundInstance.master(
                            SoundEvent.of(Identifier.of("minecraft", id)), 1.0f)));
            Draw.playTriangle(ctx, x + w - 20, ly + 6, 8, Draw.CYAN);
            ly += 24;
        }
        if (ly + 20 <= y + h) {
            Draw.scaled(ctx, tr, "Plays at your Master Volume.", x + 8, ly + 4, Draw.GRAY, 0.75f);
        }
    }

    private static void multiplayer(SupersonicScreen s, DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, int mx, int my) {
        MinecraftClient mc = MinecraftClient.getInstance();
        card(ctx, x, y, w, h);
        Draw.text(ctx, tr, "SERVER INFO", x + 8, y + 8, Draw.CYAN);

        ServerInfo info = mc.getCurrentServerEntry();
        int ly = y + 24;
        if (info != null) {
            Draw.text(ctx, tr, tr.trimToWidth(info.name, w - 16), x + 8, ly, Draw.WHITE);
            ly += 12;
            Draw.scaled(ctx, tr, tr.trimToWidth(info.address, (int) ((w - 16) / 0.8f)), x + 8, ly, Draw.GRAY, 0.8f);
            ly += 11;
            Draw.text(ctx, tr, "Ping: " + info.ping + " ms", x + 8, ly, Draw.GREEN);
            ly += 14;
        } else {
            Draw.text(ctx, tr, mc.isInSingleplayer() ? "Singleplayer world" : "Not connected", x + 8, ly, Draw.WHITE);
            ly += 14;
        }

        if (mc.getNetworkHandler() != null) {
            List<PlayerListEntry> players = new ArrayList<>(mc.getNetworkHandler().getPlayerList());
            Draw.text(ctx, tr, "ONLINE: " + players.size(), x + 8, ly, Draw.CYAN);
            ly += 12;
            int shown = 0;
            for (PlayerListEntry e : players) {
                if (shown >= 6 || ly + 10 > y + h - 60) break;
                String name = e.getProfile().getName();
                String ping = e.getLatency() + "ms";
                Draw.scaled(ctx, tr, tr.trimToWidth(name, (int) ((w - 60) / 0.85f)), x + 8, ly, Draw.WHITE, 0.85f);
                Draw.scaled(ctx, tr, ping, x + w - 8 - (int) (tr.getWidth(ping) * 0.8f), ly, Draw.GREEN, 0.8f);
                ly += 11;
                shown++;
            }
            if (players.size() > shown) {
                Draw.scaled(ctx, tr, "... and " + (players.size() - shown) + " more", x + 8, ly, Draw.GRAY, 0.8f);
            }
        }

        int by = y + h - 50;
        Draw.text(ctx, tr, "QUICK CONNECT", x + 8, by - 12, Draw.CYAN);
        s.drawButton(ctx, x + 8, by, w - 16, 18, "Multiplayer Menu", false, mx, my,
                () -> mc.setScreen(new MultiplayerScreen(s)));
        s.drawButton(ctx, x + 8, by + 22, w - 16, 18, "Singleplayer Worlds", false, mx, my,
                () -> mc.setScreen(new SelectWorldScreen(s)));
    }

    private static void controls(SupersonicScreen s, DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, int mx, int my) {
        MinecraftClient mc = MinecraftClient.getInstance();
        card(ctx, x, y, w, h);
        Draw.text(ctx, tr, "KEYBINDS", x + 8, y + 8, Draw.CYAN);
        Draw.scaled(ctx, tr, "Open the full list to rebind", x + 8, y + 22, Draw.GRAY, 0.8f);
        Draw.scaled(ctx, tr, "any key. Changes are saved.", x + 8, y + 32, Draw.GRAY, 0.8f);

        s.drawButton(ctx, x + 8, y + 50, w - 16, 18, "Open Keybinds", true, mx, my,
                () -> mc.setScreen(new net.minecraft.client.gui.screen.option.KeybindsScreen(s, mc.options)));
        s.drawButton(ctx, x + 8, y + 72, w - 16, 18, "Reset All Keybinds", false, mx, my, () -> {
            for (KeyBinding kb : mc.options.allKeys) {
                kb.setBoundKey(kb.getDefaultKey());
            }
            KeyBinding.updateKeysByCode();
            mc.options.write();
        });
        Draw.scaled(ctx, tr, "Supersonic menu: Ctrl + R-Shift", x + 8, y + 100, Draw.GRAY, 0.8f);
    }

    private static void voice(SupersonicScreen s, DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, int mx, int my) {
        card(ctx, x, y, w, h);
        Draw.text(ctx, tr, "VOICE CHAT", x + 8, y + 8, Draw.CYAN);

        boolean on = VoiceManager.isOn();
        Draw.round(ctx, x + 8, y + 24, w - 16, 26, on ? 0xFF0F5C86 : Draw.ROW);
        Draw.outline(ctx, x + 8, y + 24, w - 16, 26, on ? Draw.CYAN : Draw.CYAN_DIM);
        Draw.centered(ctx, tr, on ? "MIC ON" : "MIC OFF", x + w / 2, y + 33, on ? Draw.WHITE : Draw.GRAY);

        // Live level meter (real microphone level)
        int mxl = x + 8;
        int myl = y + 58;
        int mw = w - 16;
        Draw.rect(ctx, mxl, myl, mw, 6, 0xFF1A2A3C);
        int filled = (int) (mw * Math.min(1f, VoiceManager.level()));
        for (int i = 0; i < filled; i++) {
            float u = i / (float) Math.max(1, mw - 1);
            int r = (int) (0x4F + (0xB4 - 0x4F) * u);
            int g = (int) (0x8F + (0x8C - 0x8F) * u);
            int b = (int) (0xF0 + (0xF5 - 0xF0) * u);
            ctx.fill(mxl + i, myl, mxl + i + 1, myl + 6, 0xFF000000 | (r << 16) | (g << 8) | b);
        }
        Draw.scaled(ctx, tr, "Status: " + VoiceManager.status(), x + 8, y + 70, Draw.GRAY, 0.8f);

        int ly = y + 86;
        Draw.text(ctx, tr, "LAST HEARD", x + 8, ly, Draw.CYAN);
        ly += 12;
        String heard = VoiceManager.lastHeard();
        if (heard.isEmpty()) {
            Draw.scaled(ctx, tr, "Nothing yet.", x + 8, ly, Draw.GRAY, 0.85f);
            ly += 11;
        } else {
            for (String line : Draw.wrap(tr, heard, (int) ((w - 16) / 0.85f), 4)) {
                Draw.scaled(ctx, tr, line, x + 8, ly, Draw.WHITE, 0.85f);
                ly += 10;
            }
        }

        int by = y + h - 62;
        if (by > ly + 4) {
            s.drawButton(ctx, x + 8, by, w - 16, 18, on ? "Turn Mic Off" : "Turn Mic On", true, mx, my, VoiceManager::toggle);
            Draw.scaled(ctx, tr, "Hotkey: Left Shift + V", x + 8, by + 24, Draw.GRAY, 0.8f);
            Draw.scaled(ctx, tr, "Singleplayer / OP: commands run", x + 8, by + 34, Draw.GRAY, 0.75f);
            Draw.scaled(ctx, tr, "on the server automatically.", x + 8, by + 43, Draw.GRAY, 0.75f);
        }
    }

    private static void capture(SupersonicScreen s, DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, int mx, int my) {
        card(ctx, x, y, w, h);
        int sub = s.captureSub;
        String title;
        String dir;
        String ext;
        switch (sub) {
            case 0 -> {
                title = "REPLAY MANAGEMENT";
                dir = "replay_recordings";
                ext = ".mcpr";
            }
            case 2 -> {
                title = "RECENT SCREENSHOTS";
                dir = "screenshots";
                ext = ".png";
            }
            default -> {
                title = sub == 3 ? "CLIPBOARD" : (sub == 1 ? "RECORDING" : "LIVE STREAM");
                dir = null;
                ext = null;
            }
        }
        Draw.text(ctx, tr, title, x + 8, y + 8, Draw.CYAN);

        if (dir == null) {
            int ly = y + 26;
            if (sub == 3) {
                Draw.scaled(ctx, tr, "Last copied:", x + 8, ly, Draw.GRAY, 0.8f);
                ly += 11;
                String last = CaptureTools.lastCopied.isEmpty() ? "-" : CaptureTools.lastCopied;
                for (String line : Draw.wrap(tr, last, (int) ((w - 16) / 0.9f), 4)) {
                    Draw.scaled(ctx, tr, line, x + 8, ly, Draw.WHITE, 0.9f);
                    ly += 10;
                }
            } else {
                for (String line : Draw.wrap(tr, "Minecraft has no built-in video recorder or streamer. OBS Studio is free and works great with it.", (int) ((w - 16) / 0.85f), 6)) {
                    Draw.scaled(ctx, tr, line, x + 8, ly, Draw.GRAY, 0.85f);
                    ly += 10;
                }
                s.drawButton(ctx, x + 8, ly + 6, w - 16, 18, "Get OBS Studio", false, mx, my,
                        () -> Pages.openUrl("https://obsproject.com"));
            }
            return;
        }

        final String dirName = dir;
        s.drawButton(ctx, x + 8, y + 24, w - 16, 18, "Open Folder", false, mx, my, () -> Pages.openFolder(dirName));
        int ly = y + 50;
        List<File> list = CaptureTools.recent(dir, ext, 6);
        if (list.isEmpty()) {
            Draw.scaled(ctx, tr, "No files yet.", x + 8, ly, Draw.GRAY, 0.85f);
        }
        for (File f : list) {
            if (ly + 24 > y + h) break;
            Draw.round(ctx, x + 6, ly, w - 12, 22, Draw.ROW);
            Draw.scaled(ctx, tr, tr.trimToWidth(f.getName(), (int) ((w - 24) / 0.85f)), x + 11, ly + 3, Draw.WHITE, 0.85f);
            Draw.scaled(ctx, tr, CaptureTools.size(f.length()) + "  " + CaptureTools.date(f.lastModified()),
                    x + 11, ly + 13, Draw.GRAY, 0.75f);
            final File file = f;
            if (sub == 2) {
                s.drawButton(ctx, x + w - 26, ly + 3, 16, 16, "", false, mx, my, () -> Util.getOperatingSystem().open(file));
                Draw.playTriangle(ctx, x + w - 21, ly + 7, 7, Draw.CYAN);
            }
            ly += 25;
        }
        if (sub == 0 && !FabricLoader.getInstance().isModLoaded("replaymod") && ly + 40 < y + h) {
            Draw.scaled(ctx, tr, "Install Replay Mod to record", x + 8, ly + 6, Draw.GRAY, 0.8f);
            Draw.scaled(ctx, tr, "and play back replays.", x + 8, ly + 16, Draw.GRAY, 0.8f);
        }
    }

    private static void mods(SupersonicScreen s, DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, int mx, int my) {
        card(ctx, x, y, w, h);
        Draw.text(ctx, tr, "MOD DETAILS", x + 8, y + 8, Draw.CYAN);
        if (s.mods.isEmpty()) return;
        int idx = Math.max(0, Math.min(s.selectedMod, s.mods.size() - 1));
        ModContainer mod = s.mods.get(idx);
        ModMetadata md = mod.getMetadata();

        int ly = y + 24;
        Draw.text(ctx, tr, tr.trimToWidth(md.getName(), w - 50), x + 8, ly, Draw.WHITE);
        Draw.scaled(ctx, tr, "ACTIVE", x + w - 8 - (int) (tr.getWidth("ACTIVE") * 0.8f), ly + 1, Draw.GREEN, 0.8f);
        ly += 12;
        Draw.scaled(ctx, tr, "v" + md.getVersion().getFriendlyString() + "  |  " + md.getId(), x + 8, ly, Draw.GRAY, 0.8f);
        ly += 11;
        StringBuilder authors = new StringBuilder();
        for (Person p : md.getAuthors()) {
            if (authors.length() > 0) authors.append(", ");
            authors.append(p.getName());
        }
        if (authors.length() > 0) {
            Draw.scaled(ctx, tr, tr.trimToWidth("By " + authors, (int) ((w - 16) / 0.8f)), x + 8, ly, Draw.GRAY, 0.8f);
            ly += 11;
        }
        ly += 3;
        for (String line : Draw.wrap(tr, md.getDescription(), (int) ((w - 16) / 0.8f), 6)) {
            Draw.scaled(ctx, tr, line, x + 8, ly, Draw.WHITE, 0.8f);
            ly += 9;
        }

        int by = y + h - 64;
        Optional<String> home = md.getContact().get("homepage");
        s.drawButton(ctx, x + 8, by, w - 16, 16, "Website", false, mx, my, () -> {
            if (home.isPresent()) {
                try {
                    Util.getOperatingSystem().open(URI.create(home.get()));
                } catch (Exception ignored) {
                    // invalid link in mod metadata
                }
            }
        });
        s.drawButton(ctx, x + 8, by + 20, w - 16, 16, "Open Mods Folder", false, mx, my, () -> {
            File f = FabricLoader.getInstance().getGameDir().resolve("mods").toFile();
            f.mkdirs();
            Util.getOperatingSystem().open(f);
        });
        s.drawButton(ctx, x + 8, by + 40, w - 16, 16, "Browse Modrinth", false, mx, my, () ->
                Util.getOperatingSystem().open(URI.create("https://modrinth.com/mods")));
    }
}
