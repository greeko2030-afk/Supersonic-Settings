package net.supersonic.voice;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;

import java.util.Arrays;
import java.util.Random;

/** Futuristic holographic voice overlay shown at the top of the screen while the microphone is on. */
public final class VoiceOverlay {
    private static float alpha = 0f;
    private static long lastNs = 0L;

    // Equalizer bars
    private static final int BARS = 8;
    private static final float[] barH = new float[BARS];

    // Floating particles
    private static final int MAX_P = 20;
    private static final float[] px   = new float[MAX_P];
    private static final float[] py   = new float[MAX_P];
    private static final float[] pvx  = new float[MAX_P];
    private static final float[] pvy  = new float[MAX_P];
    private static final float[] pLif = new float[MAX_P];
    private static final Random  pRnd = new Random();

    static { Arrays.fill(pLif, -1f); }

    private VoiceOverlay() {}

    // ── Colour helpers ───────────────────────────────────────────────────────

    private static int argb(float a, int rgb) {
        int ia = Math.max(0, Math.min(255, (int)(a * 255f)));
        return (ia << 24) | (rgb & 0xFFFFFF);
    }

    private static int lerpRgb(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return (lerp8((a>>16)&0xFF, (b>>16)&0xFF, t) << 16)
             | (lerp8((a>> 8)&0xFF, (b>> 8)&0xFF, t) <<  8)
             |  lerp8( a     &0xFF,  b     &0xFF, t);
    }
    private static int lerp8(int a, int b, float t) { return (int)(a + (b - a) * t); }

    // ── Shape helpers ────────────────────────────────────────────────────────

    /** Hollow ring drawn row-by-row (no extra allocation). */
    private static void ring(DrawContext ctx, int cx, int cy, int r, int thick, int color) {
        int r2  = r  * r;
        int ri  = Math.max(0, r - thick);
        int ri2 = ri * ri;
        for (int y = -r; y <= r; y++) {
            int y2 = y * y;
            int xo = (int) Math.sqrt(Math.max(0d, r2  - y2));
            if (y2 > ri2) {
                ctx.fill(cx - xo, cy + y, cx + xo + 1, cy + y + 1, color);
            } else {
                int xi = (int) Math.sqrt(Math.max(0d, ri2 - y2));
                if (xo > xi) {
                    ctx.fill(cx - xo, cy + y, cx - xi,     cy + y + 1, color);
                    ctx.fill(cx + xi + 1, cy + y, cx + xo + 1, cy + y + 1, color);
                }
            }
        }
    }

    /** Filled disc. */
    private static void disc(DrawContext ctx, int cx, int cy, int r, int color) {
        int r2 = r * r;
        for (int y = -r; y <= r; y++) {
            int xo = (int) Math.sqrt(Math.max(0d, r2 - y * y));
            ctx.fill(cx - xo, cy + y, cx + xo + 1, cy + y + 1, color);
        }
    }

    // ── Main render ──────────────────────────────────────────────────────────

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        SupersonicConfig cfg = SupersonicClient.CONFIG;
        if (cfg == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();

        long now = System.nanoTime();
        float dt = lastNs == 0L ? 0.016f : Math.min(0.1f, (now - lastNs) / 1_000_000_000f);
        lastNs = now;

        float target = VoiceManager.isOn() && cfg.voiceAnimation ? 1f : 0f;
        alpha += (target - alpha) * Math.min(1f, dt * 10f);
        if (alpha < 0.02f) return;

        TextRenderer tr  = mc.textRenderer;
        int sw   = ctx.getScaledWindowWidth();
        int barW = Math.min(320, sw - 40);
        int x0   = (sw - barW) / 2;
        int boxH = 38;
        int y0   = 6 - (int)((1f - alpha) * (boxH + 10));
        double t = now / 1_000_000_000.0;

        boolean speaking   = VoiceManager.isSpeaking();
        boolean processing = VoiceManager.isProcessing() && !speaking;
        float   lvl    = VoiceManager.level();
        float   energy = speaking ? 0.4f + 0.6f * lvl : 0.08f + 0.15f * lvl;

        final int CYAN   = 0x00E5FF;
        final int PURPLE = 0xAA66FF;
        final int PINK   = 0xFF4080;
        final int DARK   = 0x060C18;

        int primary = processing ? PINK : CYAN;
        int midY = y0 + boxH / 2;
        int cxM  = x0 + barW / 2;

        // ── Soft outer glow ──────────────────────────────────────────────────
        for (int i = 5; i >= 1; i--) {
            int g = i * 3;
            ctx.fill(x0 - g, y0 - g/2, x0 + barW + g, y0 + boxH + g/2,
                     argb(0.04f * alpha / i, primary));
        }

        // ── Dark pill (stepped corners = rounded look) ───────────────────────
        ctx.fill(x0 + 5, y0,     x0 + barW - 5, y0 + boxH,     argb(0.9f * alpha, DARK));
        ctx.fill(x0 + 3, y0 + 2, x0 + barW - 3, y0 + boxH - 2, argb(0.9f * alpha, DARK));
        ctx.fill(x0 + 1, y0 + 4, x0 + barW - 1, y0 + boxH - 4, argb(0.9f * alpha, DARK));
        ctx.fill(x0,     y0 + 5, x0 + barW,     y0 + boxH - 5, argb(0.9f * alpha, DARK));

        // ── Top/bottom gradient border lines ────────────────────────────────
        for (int c = 0; c < barW; c++) {
            float u = c / (float)(barW - 1);
            ctx.fill(x0+c, y0+5,      x0+c+1, y0+6,      argb(0.95f*alpha, lerpRgb(CYAN, PURPLE, u)));
            ctx.fill(x0+c, y0+boxH-6, x0+c+1, y0+boxH-5, argb(0.95f*alpha, lerpRgb(PURPLE, CYAN, u)));
        }

        // ── Corner bracket decorations ───────────────────────────────────────
        int bl = 11;
        // top-left
        ctx.fill(x0,       y0+5,   x0+bl, y0+7,    argb(alpha, CYAN));
        ctx.fill(x0,       y0+5,   x0+2,  y0+5+bl, argb(alpha, CYAN));
        // top-right
        ctx.fill(x0+barW-bl, y0+5, x0+barW,   y0+7,    argb(alpha, CYAN));
        ctx.fill(x0+barW-2,  y0+5, x0+barW,   y0+5+bl, argb(alpha, CYAN));
        // bottom-left
        ctx.fill(x0,       y0+boxH-7, x0+bl, y0+boxH-5,    argb(alpha, PURPLE));
        ctx.fill(x0,       y0+boxH-5-bl, x0+2, y0+boxH-5,  argb(alpha, PURPLE));
        // bottom-right
        ctx.fill(x0+barW-bl, y0+boxH-7, x0+barW, y0+boxH-5,    argb(alpha, PURPLE));
        ctx.fill(x0+barW-2, y0+boxH-5-bl, x0+barW, y0+boxH-5,  argb(alpha, PURPLE));

        // ── Horizontal scan line sweeping across ─────────────────────────────
        int scanX = x0 + (int)(((t * 0.55) % 1.0) * barW);
        for (int s = -20; s <= 20; s++) {
            float sa = (float)(1.0 - Math.abs(s) / 20.0) * 0.3f * alpha;
            int sx = scanX + s;
            if (sx > x0 && sx < x0 + barW - 1)
                ctx.fill(sx, y0+6, sx+1, y0+boxH-6, argb(sa, CYAN));
        }

        // ── Left equalizer bars ──────────────────────────────────────────────
        int eqMargin = 14;
        int eqEndL   = cxM - 22;
        int eqStaL   = x0  + eqMargin;
        int eqZone   = eqEndL - eqStaL;
        int bSlot    = Math.max(5, eqZone / BARS);
        int maxBH    = boxH / 2 - 7;

        for (int i = 0; i < BARS; i++) {
            double wave = 0.5 + 0.5 * Math.sin(t * (2.0 + i * 1.1) + i * 0.9);
            float bt = (float)(energy * (0.55 * wave + 0.45));
            barH[i] += (bt - barH[i]) * Math.min(1f, dt * 18f);
            int bh  = Math.max(1, (int)(barH[i] * maxBH));
            int bx  = eqStaL + i * bSlot + bSlot/2 - 2;
            int bw  = Math.max(3, bSlot - 3);
            int col = lerpRgb(CYAN, PURPLE, i / (float)(BARS - 1));
            ctx.fill(bx-1, midY-bh-2, bx+bw+1, midY+bh+3, argb(0.15f*alpha, col));  // glow
            ctx.fill(bx,   midY-bh,   bx+bw,   midY+bh+1, argb(0.88f*alpha, col));  // bar
            ctx.fill(bx,   midY-bh-1, bx+bw,   midY-bh,   argb(alpha, 0xFFFFFF));   // top cap
            ctx.fill(bx,   midY+bh+1, bx+bw,   midY+bh+2, argb(alpha, 0xFFFFFF));   // bot cap
        }

        // ── Right equalizer bars (mirror) ────────────────────────────────────
        int eqStaR = cxM + 22;
        for (int i = 0; i < BARS; i++) {
            int ri = BARS - 1 - i;
            int bh  = Math.max(1, (int)(barH[ri] * maxBH));
            int bx  = eqStaR + i * bSlot + bSlot/2 - 2;
            int bw  = Math.max(3, bSlot - 3);
            int col = lerpRgb(PURPLE, CYAN, i / (float)(BARS - 1));
            ctx.fill(bx-1, midY-bh-2, bx+bw+1, midY+bh+3, argb(0.15f*alpha, col));
            ctx.fill(bx,   midY-bh,   bx+bw,   midY+bh+1, argb(0.88f*alpha, col));
            ctx.fill(bx,   midY-bh-1, bx+bw,   midY-bh,   argb(alpha, 0xFFFFFF));
            ctx.fill(bx,   midY+bh+1, bx+bw,   midY+bh+2, argb(alpha, 0xFFFFFF));
        }

        // ── Centre mic icon with pulsing rings ───────────────────────────────
        float pulse  = 0.88f + 0.12f * (float)Math.sin(t * 5.2);
        float pulse2 = 0.88f + 0.12f * (float)Math.sin(t * 5.2 + Math.PI);
        ring(ctx, cxM, midY, (int)(17 * pulse),  1, argb((0.12f + 0.10f*energy)*alpha, primary));
        ring(ctx, cxM, midY, (int)(13 * pulse2), 1, argb((0.18f + 0.15f*energy)*alpha, primary));
        disc(ctx, cxM, midY, 8, argb(0.22f*alpha, primary));     // soft glow disc
        disc(ctx, cxM, midY, 6, argb(0.78f*alpha, primary));     // main disc
        disc(ctx, cxM, midY, 3, argb(alpha,        0xFFFFFF));   // bright core
        ctx.fill(cxM-1, midY+6, cxM+2, midY+10, argb(0.9f*alpha, primary));  // stand
        ctx.fill(cxM-5, midY+10, cxM+6, midY+12, argb(0.9f*alpha, primary)); // base

        // ── Rising particles when speaking ───────────────────────────────────
        for (int i = 0; i < MAX_P; i++) {
            if (pLif[i] <= 0f) {
                if (!speaking) continue;
                px[i]   = x0 + 10 + pRnd.nextInt(Math.max(1, barW - 20));
                py[i]   = y0 + boxH - 7f;
                pvx[i]  = (pRnd.nextFloat() - 0.5f) * 24f;
                pvy[i]  = -(pRnd.nextFloat() * 35f + 15f);
                pLif[i] = 0.5f + pRnd.nextFloat() * 0.7f;
            } else {
                pLif[i] -= dt * (0.55f + 0.45f * lvl);
                px[i]   += pvx[i] * dt;
                py[i]   += pvy[i] * dt;
                int ipx = (int) px[i], ipy = (int) py[i];
                if (ipx >= x0-4 && ipx < x0+barW+4 && ipy >= y0-28 && ipy < y0+boxH+5)
                    ctx.fill(ipx, ipy, ipx+2, ipy+2, argb(pLif[i] * 0.85f * alpha, primary));
            }
        }

        // ── Status label ─────────────────────────────────────────────────────
        String label = speaking ? "> LISTENING" : (processing ? "> TRANSCRIBING" : "> STANDBY");
        int labelCol  = speaking ? 0x00E5FF : (processing ? 0xFF4080 : 0x506880);
        int lw = tr.getWidth(label);
        ctx.drawTextWithShadow(tr, label, (sw - lw) / 2, y0 + boxH + 4, argb(alpha, labelCol));

        // ── Last heard text ───────────────────────────────────────────────────
        long since = System.currentTimeMillis() - VoiceManager.lastHeardAt();
        String heard = VoiceManager.lastHeard();
        if (!heard.isEmpty() && since < 4000L) {
            float fade = since < 3000L ? 1f : 1f - (since - 3000L) / 1000f;
            String s = tr.trimToWidth("\u00bb " + heard, Math.min(sw - 20, 320));
            ctx.drawTextWithShadow(tr, s, (sw - tr.getWidth(s)) / 2, y0 + boxH + 15,
                    argb(fade * alpha * 0.75f, 0xBBCCFF));
        }
    }
}
