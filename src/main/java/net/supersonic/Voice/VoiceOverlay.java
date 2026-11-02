package net.supersonic.voice;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;

/** Animated gradient voice bar shown at the top of the screen while the microphone is on. */
public final class VoiceOverlay {
    private static float alpha = 0f;
    private static long lastNs = 0L;

    private VoiceOverlay() {
    }

    private static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static int argb(float a, int rgb) {
        int ia = Math.max(0, Math.min(255, (int) (a * 255f)));
        return (ia << 24) | (rgb & 0xFFFFFF);
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        SupersonicConfig cfg = SupersonicClient.CONFIG;
        if (cfg == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();

        long now = System.nanoTime();
        float dt = lastNs == 0L ? 0.016f : Math.min(0.1f, (now - lastNs) / 1_000_000_000f);
        lastNs = now;

        float target = VoiceManager.isOn() && cfg.voiceAnimation ? 1f : 0f;
        alpha += (target - alpha) * Math.min(1f, dt * 8f);
        if (alpha < 0.02f) return;

        TextRenderer tr = mc.textRenderer;
        int sw = ctx.getScaledWindowWidth();
        int barW = Math.min(260, sw - 40);
        int x0 = (sw - barW) / 2;
        int boxH = 22;
        int y0 = 6 - (int) ((1f - alpha) * 24f);
        double t = now / 1_000_000_000.0;

        boolean speaking = VoiceManager.isSpeaking();
        boolean processing = VoiceManager.isProcessing() && !speaking;
        float lvl = VoiceManager.level();
        float energy = speaking ? 0.35f + 0.65f * lvl : 0.12f + 0.25f * lvl;

        final int blue = 0x4F8FF0;
        final int purple = 0xB48CF5;

        // Soft glow
        for (int i = 4; i >= 1; i--) {
            int g = i * 3;
            ctx.fill(x0 - g, y0 - g / 2, x0 + barW + g, y0 + boxH + g / 2, argb(0.05f * alpha, lerp(blue, purple, 0.5f)));
        }
        // Dark pill (stepped corners)
        int dark = argb(0.82f * alpha, 0x0B1220);
        ctx.fill(x0 + 3, y0, x0 + barW - 3, y0 + boxH, dark);
        ctx.fill(x0 + 1, y0 + 2, x0 + barW - 1, y0 + boxH - 2, dark);
        ctx.fill(x0, y0 + 4, x0 + barW, y0 + boxH - 4, dark);

        // Animated gradient waves
        int mid = y0 + boxH / 2;
        for (int c = 6; c < barW - 6; c++) {
            float u = c / (float) (barW - 1);
            double w1 = 0.5 + 0.5 * Math.sin(u * 13.0 + t * 5.0);
            double w2 = 0.5 + 0.5 * Math.sin(u * 7.0 - t * 3.3);
            double env = Math.pow(Math.sin(Math.PI * u), 0.7);
            float hh = (float) (1.5 + (boxH * 0.42) * env * energy * (0.6 * w1 + 0.4 * w2));
            int color = lerp(blue, purple, u);
            int top = (int) (mid - hh);
            int bot = (int) (mid + hh) + 1;
            ctx.fill(x0 + c, top, x0 + c + 1, bot, argb(0.95f * alpha, color));
        }

        // Shimmer while the transcript is being processed
        if (processing) {
            double pos = (t * 0.9) % 1.0;
            for (int c = 6; c < barW - 6; c++) {
                float u = c / (float) (barW - 1);
                double d = Math.abs(u - pos);
                if (d < 0.12) {
                    float a = (float) (1.0 - d / 0.12) * 0.55f * alpha;
                    ctx.fill(x0 + c, mid - 4, x0 + c + 1, mid + 5, argb(a, 0xFFFFFF));
                }
            }
        }

        // Label
        String label = speaking ? "Listening..." : (processing ? "Transcribing..." : "Say something");
        int lw = tr.getWidth(label);
        ctx.drawTextWithShadow(tr, label, (sw - lw) / 2, y0 + boxH + 4, argb(alpha, 0xFFFFFF));

        // Last heard text (4 seconds)
        long since = System.currentTimeMillis() - VoiceManager.lastHeardAt();
        String heard = VoiceManager.lastHeard();
        if (!heard.isEmpty() && since < 4000L) {
            String s = tr.trimToWidth(heard, Math.min(sw - 20, 320));
            ctx.drawTextWithShadow(tr, s, (sw - tr.getWidth(s)) / 2, y0 + boxH + 16, argb(alpha, 0xC9D6FF));
        }
    }
}
