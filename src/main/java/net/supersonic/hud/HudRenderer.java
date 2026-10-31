package net.supersonic.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HudRenderer {
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private HudRenderer() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        FrameStats.frame();
        CpsCounter.poll(mc.getWindow().getHandle());

        SupersonicConfig cfg = SupersonicClient.CONFIG;
        if (cfg == null || !cfg.hudEnabled) return;
        if (mc.player == null || mc.world == null) return;
        if (mc.options.hudHidden) return;
        if (mc.inGameHud.getDebugHud().shouldShowDebugHud()) return; // F3 is open

        List<String> lines = buildLines(mc, cfg);
        if (lines.isEmpty() && !cfg.showArmor) return;

        int boxW = boxWidth(mc, cfg, lines);
        int boxH = boxHeight(cfg, lines);

        float s = cfg.hudScale;
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        boolean right = cfg.corner == SupersonicConfig.Corner.TOP_RIGHT
                || cfg.corner == SupersonicConfig.Corner.BOTTOM_RIGHT;
        boolean bottom = cfg.corner == SupersonicConfig.Corner.BOTTOM_LEFT
                || cfg.corner == SupersonicConfig.Corner.BOTTOM_RIGHT;

        int x = right ? sw - (int) (boxW * s) - cfg.offsetX : cfg.offsetX;
        int y = bottom ? sh - (int) (boxH * s) - cfg.offsetY : cfg.offsetY;

        drawBox(ctx, mc, cfg, x, y, s, lines);
    }

    public static List<String> buildLines(MinecraftClient mc, SupersonicConfig cfg) {
        List<String> lines = new ArrayList<>();
        if (mc.player == null || mc.world == null) return lines;
        String a = cfg.accent.code;

        if (cfg.showFps) {
            lines.add(a + "FPS §f" + mc.getCurrentFps());
        }
        if (cfg.showCps) {
            lines.add(a + "CPS §f" + CpsCounter.left() + " §7|§f " + CpsCounter.right());
        }
        if (cfg.showCoordinates) {
            lines.add(String.format(Locale.ROOT, "%sXYZ §f%.1f %.1f %.1f", a,
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
        if (cfg.showDirection) {
            Direction facing = mc.player.getHorizontalFacing();
            String name = facing.asString();
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            float yaw = MathHelper.wrapDegrees(mc.player.getYaw());
            lines.add(String.format(Locale.ROOT, "%sFacing §f%s (%.0f°)", a, name, yaw));
        }
        if (cfg.showBiome) {
            String biome = mc.world.getBiome(mc.player.getBlockPos()).getKey()
                    .map(k -> k.getValue().getPath().replace('_', ' '))
                    .orElse("unknown");
            lines.add(a + "Biome §f" + biome);
        }
        if (cfg.showClock) {
            long t = mc.world.getTimeOfDay();
            long dayTime = t % 24000L;
            int hour = (int) ((dayTime / 1000L + 6L) % 24L);
            int minute = (int) ((dayTime % 1000L) * 60L / 1000L);
            lines.add(String.format(Locale.ROOT, "%sTime §f%02d:%02d §7(Day %d)", a, hour, minute, t / 24000L + 1L));
        }
        if (cfg.showMemory) {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
            long max = rt.maxMemory() / 1048576L;
            lines.add(a + "RAM §f" + used + " / " + max + " MB");
        }
        if (cfg.showPotions) {
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                String name = e.getEffectType().value().getName().getString();
                int amp = Math.max(0, Math.min(e.getAmplifier(), ROMAN.length - 1));
                String time;
                if (e.isInfinite()) {
                    time = "∞";
                } else {
                    int secs = e.getDuration() / 20;
                    time = String.format(Locale.ROOT, "%d:%02d", secs / 60, secs % 60);
                }
                lines.add(a + name + " " + ROMAN[amp] + " §f" + time);
            }
        }
        return lines;
    }

    public static int boxWidth(MinecraftClient mc, SupersonicConfig cfg, List<String> lines) {
        int textW = 0;
        for (String l : lines) {
            textW = Math.max(textW, mc.textRenderer.getWidth(l));
        }
        if (cfg.showArmor) textW = Math.max(textW, 4 * 18);
        return textW + 10;
    }

    public static int boxHeight(SupersonicConfig cfg, List<String> lines) {
        return lines.size() * 10 + 6 + (cfg.showArmor ? 20 : 0);
    }

    /** Draws the HUD box with its top-left corner at (x, y). Used by the real HUD and the settings preview. */
    public static void drawBox(DrawContext ctx, MinecraftClient mc, SupersonicConfig cfg,
                               int x, int y, float scale, List<String> lines) {
        TextRenderer tr = mc.textRenderer;
        int boxW = boxWidth(mc, cfg, lines);
        int boxH = boxHeight(cfg, lines);

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(x, y, 0);
        m.scale(scale, scale, 1.0f);

        if (cfg.showBackground) {
            int alpha = Math.max(0, Math.min(255, (int) (cfg.bgOpacity * 255f)));
            ctx.fill(0, 0, boxW, boxH, alpha << 24);
        }
        ctx.fill(0, 0, 2, boxH, cfg.accent.argb);

        int ty = 4;
        for (String l : lines) {
            ctx.drawTextWithShadow(tr, l, 7, ty, 0xFFFFFF);
            ty += 10;
        }

        if (cfg.showArmor && mc.player != null) {
            int ay = ty + 1;
            for (int i = 0; i < ARMOR.length; i++) {
                ItemStack stack = mc.player.getEquippedStack(ARMOR[i]);
                if (!stack.isEmpty()) {
                    ctx.drawItemInSlot(tr, stack, 7 + i * 18, ay);
                }
            }
        }
        m.pop();
    }
}
