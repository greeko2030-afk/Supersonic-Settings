package net.supersonic.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class SupersonicScreen extends Screen {

    private enum Tab {
        GAMEPLAY("Gameplay"),
        VISUALS("Visuals"),
        PERFORMANCE("Performance"),
        HUD("HUD & Overlay"),
        AUDIO("Audio"),
        CONTROLS("Controls"),
        MODS("Mods & Addons");

        final String title;

        Tab(String title) {
            this.title = title;
        }
    }

    private static final int CYAN = 0xFF00D4FF;
    private static final int ROW_H = 22;

    private final Screen parent;
    private Tab tab = Tab.GAMEPLAY;

    private int px, py, pw, ph;
    private int sideW, contentX, contentW, contentTop, contentBottom, colW;
    private int widgetIndex;
    private int modScroll = 0;
    private List<String> modLines = new ArrayList<>();

    public SupersonicScreen(Screen parent) {
        super(Text.literal("Supersonic Settings"));
        this.parent = parent;
    }

    // ------------------------------------------------------------------ layout

    @Override
    protected void init() {
        pw = Math.min(this.width - 16, 520);
        ph = Math.min(this.height - 16, 310);
        px = (this.width - pw) / 2;
        py = (this.height - ph) / 2;

        sideW = 112;
        contentX = px + sideW + 18;
        contentW = pw - sideW - 28;
        contentTop = py + 42;
        contentBottom = py + ph - 32;
        colW = (contentW - 6) / 2;
        widgetIndex = 0;

        // Sidebar tabs
        int ty = py + 42;
        for (Tab t : Tab.values()) {
            String label = (t == tab ? "> " : "") + t.title;
            ButtonWidget b = ButtonWidget.builder(Text.literal(label), btn -> {
                tab = t;
                modScroll = 0;
                this.clearAndInit();
            }).dimensions(px + 8, ty, sideW, 20).build();
            this.addDrawableChild(b);
            ty += ROW_H;
        }

        // Bottom buttons
        int by = py + ph - 26;
        int bw = 64;
        int bx = px + pw - 8 - bw * 3 - 8;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), b -> applyAll())
                .dimensions(bx, by, bw, 18).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset HUD"), b -> {
            SupersonicClient.CONFIG.resetHud();
            applyAll();
            this.clearAndInit();
        }).dimensions(bx + bw + 4, by, bw, 18).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> this.close())
                .dimensions(bx + (bw + 4) * 2, by, bw, 18).build());

        buildTab();
    }

    private void buildTab() {
        GameOptions o = this.client.options;
        SupersonicConfig cfg = SupersonicClient.CONFIG;

        switch (tab) {
            case GAMEPLAY -> {
                addOption(o.getAttackIndicator());
                addOption(o.getAutoJump());
                addOption(o.getSprintToggled());
                addOption(o.getSneakToggled());
                addOption(o.getBobView());
                addOption(o.getFov());
                addOption(o.getFovEffectScale());
                addOption(o.getDistortionEffectScale());
                addOption(o.getMouseSensitivity());
                addOption(o.getShowSubtitles());
            }
            case VISUALS -> {
                addOption(o.getGraphicsMode());
                addOption(o.getAo());
                addOption(o.getCloudRenderMode());
                addOption(o.getParticles());
                addOption(o.getBiomeBlendRadius());
                addOption(o.getEntityShadows());
                addOption(o.getGamma());
                addOption(o.getMipmapLevels());
                addOption(o.getEntityDistanceScaling());
                addOption(o.getEnableVsync());
            }
            case PERFORMANCE -> {
                addOption(o.getViewDistance());
                addOption(o.getSimulationDistance());
                addOption(o.getMaxFps());
                addOption(o.getChunkBuilderMode());

                int y = contentTop + (widgetIndex + 1) / 2 * ROW_H + 4;
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Optimize Now (safe vanilla tweaks)"),
                        b -> {
                            optimizeNow();
                            this.clearAndInit();
                        }).dimensions(contentX, y, contentW, 20).build());
            }
            case HUD -> {
                addToggle("HUD Enabled", cfg.hudEnabled, v -> cfg.hudEnabled = v);
                addToggle("FPS Counter", cfg.showFps, v -> cfg.showFps = v);
                addToggle("CPS Counter", cfg.showCps, v -> cfg.showCps = v);
                addToggle("Coordinates", cfg.showCoordinates, v -> cfg.showCoordinates = v);
                addToggle("Direction", cfg.showDirection, v -> cfg.showDirection = v);
                addToggle("Clock", cfg.showClock, v -> cfg.showClock = v);
                addToggle("RAM Usage", cfg.showMemory, v -> cfg.showMemory = v);
                addToggle("Background", cfg.showBackground, v -> cfg.showBackground = v);
                addToggle("F3: Supersonic Engine", cfg.f3Info, v -> cfg.f3Info = v);

                addWidget(CyclingButtonWidget.<SupersonicConfig.Corner>builder(c -> Text.literal(c.label))
                        .values(SupersonicConfig.Corner.values())
                        .initially(cfg.corner)
                        .build(0, 0, colW, 20, Text.literal("Alignment"), (b, v) -> cfg.corner = v));

                addWidget(new FloatSlider(0, 0, colW, "HUD Scale", 0.5f, 2.0f, cfg.hudScale, "%.2f",
                        v -> cfg.hudScale = v));
                addWidget(new FloatSlider(0, 0, colW, "X Offset", 0f, 200f, cfg.offsetX, "%.0f",
                        v -> cfg.offsetX = Math.round(v)));
                addWidget(new FloatSlider(0, 0, colW, "Y Offset", 0f, 200f, cfg.offsetY, "%.0f",
                        v -> cfg.offsetY = Math.round(v)));
            }
            case AUDIO -> {
                for (SoundCategory cat : SoundCategory.values()) {
                    addOption(o.getSoundVolumeOption(cat));
                }
            }
            case CONTROLS -> {
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Open Keybinds"),
                        b -> this.client.setScreen(new KeybindsScreen(this, this.client.options)))
                        .dimensions(contentX, contentTop, contentW, 20).build());
                addOption(o.getMouseSensitivity());
                addOption(o.getInvertYMouse());
                addOption(o.getDiscreteMouseScroll());
                addOption(o.getAutoJump());
            }
            case MODS -> {
                modLines = new ArrayList<>();
                List<ModContainer> mods = new ArrayList<>(FabricLoader.getInstance().getAllMods());
                mods.sort(Comparator.comparing(m -> m.getMetadata().getName().toLowerCase(Locale.ROOT)));
                for (ModContainer m : mods) {
                    String id = m.getMetadata().getId();
                    if (id.startsWith("fabric-") || id.equals("java")) continue; // hide API sub-modules
                    modLines.add(m.getMetadata().getName() + "  §7v" + m.getMetadata().getVersion().getFriendlyString());
                }
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Open Mods Folder"),
                        b -> net.minecraft.util.Util.getOperatingSystem().open(
                                FabricLoader.getInstance().getGameDir().resolve("mods").toFile()))
                        .dimensions(contentX, contentBottom - 20, contentW, 18).build());
            }
        }
    }

    // ----------------------------------------------------------------- helpers

    private void addOption(SimpleOption<?> option) {
        int col = widgetIndex % 2;
        int row = widgetIndex / 2;
        ClickableWidget w = option.createWidget(this.client.options,
                contentX + col * (colW + 6), contentTop + row * ROW_H, colW);
        this.addDrawableChild(w);
        widgetIndex++;
    }

    private void addWidget(ClickableWidget w) {
        int col = widgetIndex % 2;
        int row = widgetIndex / 2;
        w.setX(contentX + col * (colW + 6));
        w.setY(contentTop + row * ROW_H);
        this.addDrawableChild(w);
        widgetIndex++;
    }

    private void addToggle(String label, boolean initial, Consumer<Boolean> setter) {
        addWidget(CyclingButtonWidget.onOffBuilder(initial)
                .build(0, 0, colW, 20, Text.literal(label), (b, v) -> setter.accept(v)));
    }

    /** Real vanilla option changes that raise FPS. */
    private void optimizeNow() {
        GameOptions o = this.client.options;
        o.getViewDistance().setValue(10);
        o.getSimulationDistance().setValue(8);
        o.getMaxFps().setValue(260);
        o.getEnableVsync().setValue(false);
        o.getEntityShadows().setValue(false);
        o.getGraphicsMode().setValue(GraphicsMode.FAST);
        o.getCloudRenderMode().setValue(CloudRenderMode.FAST);
        o.getParticles().setValue(ParticlesMode.DECREASED);
        o.getBiomeBlendRadius().setValue(1);
        applyAll();
    }

    private void applyAll() {
        this.client.options.write();
        SupersonicClient.CONFIG.save();
    }

    // ------------------------------------------------------------------ render

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Intentionally empty: the background is drawn in render() before the widgets.
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dim the world and draw the panel
        ctx.fill(0, 0, this.width, this.height, 0x99000000);
        ctx.fill(px, py, px + pw, py + ph, 0xF0081420);
        ctx.drawBorder(px, py, pw, ph, CYAN);
        ctx.fill(contentX - 8, py + 40, contentX - 7, py + ph - 34, 0x6600D4FF);
        ctx.fill(px + 1, py + 36, px + pw - 1, py + 37, 0x6600D4FF);

        // Header
        var m = ctx.getMatrices();
        m.push();
        m.translate(px + 10, py + 8, 0);
        m.scale(1.5f, 1.5f, 1f);
        ctx.drawTextWithShadow(this.textRenderer, "§b§lSUPERSONIC SETTINGS", 0, 0, 0xFFFFFF);
        m.pop();
        ctx.drawTextWithShadow(this.textRenderer, "Optimize. Accelerate. Dominate.", px + 10, py + 24, 0xAAAAAA);

        // Tab title
        ctx.drawTextWithShadow(this.textRenderer, "§b" + tab.title.toUpperCase(Locale.ROOT),
                px + pw - 10 - this.textRenderer.getWidth(tab.title), py + 14, 0xFFFFFF);

        // Sidebar footer
        ctx.drawTextWithShadow(this.textRenderer, "§bSupersonic Engine", px + 8, py + ph - 34, 0xFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, "§7v1.0.0 | Ctrl+RShift", px + 8, py + ph - 23, 0xFFFFFF);

        if (tab == Tab.PERFORMANCE) {
            renderLiveStats(ctx);
        } else if (tab == Tab.MODS) {
            renderModList(ctx);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderLiveStats(DrawContext ctx) {
        int y = contentTop + (widgetIndex + 1) / 2 * ROW_H + 34;
        int fps = this.client.getCurrentFps();
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;

        String gpu;
        try {
            gpu = GL11.glGetString(GL11.GL_RENDERER);
        } catch (Throwable t) {
            gpu = "unknown";
        }

        ctx.drawTextWithShadow(this.textRenderer, "§bLIVE STATS", contentX, y, 0xFFFFFF);
        y += 12;
        ctx.drawTextWithShadow(this.textRenderer, String.format(Locale.ROOT,
                "FPS: §f%d §7(%.2f ms)", fps, 1000.0 / Math.max(1, fps)), contentX, y, 0xAAAAAA);
        y += 11;
        ctx.drawTextWithShadow(this.textRenderer, "RAM: §f" + used + " / " + max + " MB", contentX, y, 0xAAAAAA);
        y += 11;
        ctx.drawTextWithShadow(this.textRenderer, "GPU: §f" + gpu, contentX, y, 0xAAAAAA);
        y += 11;
        ctx.drawTextWithShadow(this.textRenderer, "Java: §f" + System.getProperty("java.version")
                + " §7| CPU threads: §f" + rt.availableProcessors(), contentX, y, 0xAAAAAA);
    }

    private void renderModList(DrawContext ctx) {
        int top = contentTop;
        int bottom = contentBottom - 26;
        int lineH = 12;
        int visible = Math.max(1, (bottom - top) / lineH);
        int maxScroll = Math.max(0, modLines.size() - visible);
        modScroll = Math.max(0, Math.min(modScroll, maxScroll));

        ctx.drawTextWithShadow(this.textRenderer, "§bLoaded mods: §f" + modLines.size(), contentX, top - 2, 0xFFFFFF);
        top += 12;

        ctx.enableScissor(contentX, top, contentX + contentW, bottom);
        int y = top;
        for (int i = modScroll; i < modLines.size() && y < bottom; i++) {
            ctx.drawTextWithShadow(this.textRenderer, modLines.get(i), contentX + 4, y, 0xFFFFFF);
            y += lineH;
        }
        ctx.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (tab == Tab.MODS) {
            modScroll -= (int) Math.signum(verticalAmount) * 2;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void removed() {
        applyAll();
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    // ------------------------------------------------------------ slider widget

    private static class FloatSlider extends SliderWidget {
        private final String label;
        private final float min;
        private final float max;
        private final String format;
        private final Consumer<Float> setter;

        FloatSlider(int x, int y, int width, String label, float min, float max, float current,
                    String format, Consumer<Float> setter) {
            super(x, y, width, 20, Text.empty(), (current - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.format = format;
            this.setter = setter;
            this.updateMessage();
        }

        private float real() {
            return min + (float) ((max - min) * this.value);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal(label + ": " + String.format(Locale.ROOT, format, real())));
        }

        @Override
        protected void applyValue() {
            setter.accept(real());
        }
    }
}
