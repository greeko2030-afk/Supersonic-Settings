package net.supersonic.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HudRenderer {
    private HudRenderer() {
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        CpsCounter.poll(mc.getWindow().getHandle());

        SupersonicConfig cfg = SupersonicClient.CONFIG;
        if (cfg == null || !cfg.hudEnabled) return;
        if (mc.player == null || mc.world == null) return;
        if (mc.options.hudHidden) return;
        if (mc.options.debugEnabled) return; // F3 is open

        List<String> lines = new ArrayList<>();

        if (cfg.showFps) {
            lines.add("§bFPS §f" + mc.getCurrentFps());
        }
        if (cfg.showCps) {
            lines.add("§bCPS §f" + CpsCounter.left() + " §7|§f " + CpsCounter.right());
        }
        if (cfg.showCoordinates) {
            lines.add(String.format(Locale.ROOT, "§bXYZ §f%.1f %.1f %.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
        if (cfg.showDirection) {
            Direction facing = mc.player.getHorizontalFacing();
            String name = facing.asString();
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            float yaw = MathHelper.wrapDegrees(mc.player.getYaw());
            lines.add(String.format(Locale.ROOT, "§bFacing §f%s (%.0f°)", name, yaw));
        }
        if (cfg.showClock) {
            long t = mc.world.getTimeOfDay();
            long dayTime = t % 24000L;
            int hour = (int) ((dayTime / 1000L + 6L) % 24L);
            int minute = (int) ((dayTime % 1000L) * 60L / 1000L);
            lines.add(String.format(Locale.ROOT, "§bTime §f%02d:%02d §7(Day %d)", hour, minute, t / 24000L + 1L));
        }
        if (cfg.showMemory) {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
            long max = rt.maxMemory() / 1048576L;
            lines.add("§bRAM §f" + used + " / " + max + " MB");
        }

        if (lines.isEmpty()) return;

        TextRenderer tr = mc.textRenderer;
        int textW = 0;
        for (String l : lines) {
            textW = Math.max(textW, tr.getWidth(l));
        }
        int boxW = textW + 10;
        int boxH = lines.size() * 10 + 6;

        float s = cfg.hudScale;
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        boolean right = cfg.corner == SupersonicConfig.Corner.TOP_RIGHT
                || cfg.corner == SupersonicConfig.Corner.BOTTOM_RIGHT;
        boolean bottom = cfg.corner == SupersonicConfig.Corner.BOTTOM_LEFT
                || cfg.corner == SupersonicConfig.Corner.BOTTOM_RIGHT;

        int x = right ? sw - (int) (boxW * s) - cfg.offsetX : cfg.offsetX;
        int y = bottom ? sh - (int) (boxH * s) - cfg.offsetY : cfg.offsetY;

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(x, y, 0);
        m.scale(s, s, 1.0f);

        if (cfg.showBackground) {
            ctx.fill(0, 0, boxW, boxH, 0x90000000);
        }
        ctx.fill(0, 0, 2, boxH, 0xFF00D4FF);

        int ty = 4;
        for (String l : lines) {
            ctx.drawTextWithShadow(tr, l, 7, ty, 0xFFFFFF);
            ty += 10;
        }
        m.pop();
    }
}
