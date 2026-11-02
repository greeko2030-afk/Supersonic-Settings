package net.supersonic.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small drawing helpers (rounded boxes, scaled text, polygons, icons). */
public final class Draw {
    public static final int CYAN = 0xFF00D4FF;
    public static final int CYAN_DIM = 0xFF0B7FA8;
    public static final int BG = 0xF2071320;
    public static final int PANEL = 0xCC0B1B2E;
    public static final int ROW = 0x99102238;
    public static final int ROW_HOVER = 0xCC17324F;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int GRAY = 0xFF9FB3C8;
    public static final int GREEN = 0xFF3DFF7A;
    public static final int RED = 0xFFFF5555;

    private static final String[][] ICONS = {
            // 0 gameplay (gamepad)
            {"........", ".######.", "########", "##.##.##", "#..##..#", "########", ".##..##.", "........"},
            // 1 visuals (monitor)
            {"########", "#......#", "#......#", "#......#", "########", "...##...", "..####..", "........"},
            // 2 performance (bolt)
            {".....##.", "....##..", "...##...", "..######", "....##..", "...##...", "..##....", ".#......"},
            // 3 hud (grid)
            {"###.###.", "###.###.", "###.###.", "........", "###.###.", "###.###.", "###.###.", "........"},
            // 4 audio (speaker)
            {"...#....", "..##..#.", "####...#", "####.#.#", "####.#.#", "####...#", "..##..#.", "...#...."},
            // 5 multiplayer (people)
            {".##.##..", ".##.##..", "........", "####.###", "####.###", "####.###", "........", "........"},
            // 6 controls (keys)
            {"###..###", "#.#..#.#", "###..###", "........", "###..###", "#.#..#.#", "###..###", "........"},
            // 7 mods (puzzle)
            {"..###...", "..#.#...", "#######.", "#.....#.", "#.....##", "#.....#.", "#######.", "........"},
            // 8 capture (camera)
            {"..###...", "########", "#......#", "#..##..#", "#.#..#.#", "#..##..#", "########", "........"},
            // 9 voice (microphone)
            {"...##...", "..####..", "..####..", "#.####.#", "#.####.#", ".######.", "...##...", "..####.."}
    };

    private Draw() {
    }

    public static void rect(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x, y, x + w, y + h, color);
    }

    /** Box with cut corners (looks rounded). */
    public static void round(DrawContext c, int x, int y, int w, int h, int color) {
        if (w < 4 || h < 4) {
            rect(c, x, y, w, h, color);
            return;
        }
        c.fill(x + 1, y, x + w - 1, y + h, color);
        c.fill(x, y + 1, x + w, y + h - 1, color);
    }

    public static void outline(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x + 1, y, x + w - 1, y + 1, color);
        c.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
        c.fill(x, y + 1, x + 1, y + h - 1, color);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public static void text(DrawContext c, TextRenderer tr, String s, int x, int y, int color) {
        c.drawTextWithShadow(tr, s, x, y, color);
    }

    public static void centered(DrawContext c, TextRenderer tr, String s, int cx, int y, int color) {
        c.drawTextWithShadow(tr, s, cx - tr.getWidth(s) / 2, y, color);
    }

    public static void scaled(DrawContext c, TextRenderer tr, String s, int x, int y, int color, float scale) {
        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(x, y, 0);
        m.scale(scale, scale, 1.0f);
        c.drawText(tr, s, 0, 0, color, false);
        m.pop();
    }

    public static void scaledShadow(DrawContext c, TextRenderer tr, String s, int x, int y, int color, float scale) {
        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(x, y, 0);
        m.scale(scale, scale, 1.0f);
        c.drawTextWithShadow(tr, s, 0, 0, color);
        m.pop();
    }

    /** Scanline fill of a simple polygon (even-odd). */
    public static void polygon(DrawContext c, double[] xs, double[] ys, int color) {
        int n = xs.length;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (double y : ys) {
            minY = Math.min(minY, (int) Math.floor(y));
            maxY = Math.max(maxY, (int) Math.ceil(y));
        }
        for (int y = minY; y < maxY; y++) {
            double sy = y + 0.5;
            List<Double> hits = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                double y1 = ys[i];
                double y2 = ys[j];
                if ((y1 <= sy && y2 > sy) || (y2 <= sy && y1 > sy)) {
                    double t = (sy - y1) / (y2 - y1);
                    hits.add(xs[i] + t * (xs[j] - xs[i]));
                }
            }
            Collections.sort(hits);
            for (int k = 0; k + 1 < hits.size(); k += 2) {
                c.fill((int) Math.round(hits.get(k)), y, (int) Math.round(hits.get(k + 1)), y + 1, color);
            }
        }
    }

    /** Lightning bolt logo. */
    public static void bolt(DrawContext c, int x, int y, int size, int color) {
        double[] px = {58, 18, 44, 32, 82, 56, 72};
        double[] py = {0, 56, 56, 100, 38, 38, 0};
        double[] xs = new double[px.length];
        double[] ys = new double[py.length];
        for (int i = 0; i < px.length; i++) {
            xs[i] = x + px[i] * size / 100.0;
            ys[i] = y + py[i] * size / 100.0;
        }
        polygon(c, xs, ys, color);
    }

    /** Isometric cube, r = half width. */
    public static void cube(DrawContext c, int cx, int y, int r) {
        double h = r / 2.0;
        polygon(c, new double[]{cx, cx + r, cx, cx - r}, new double[]{y, y + h, y + r, y + h}, 0xFF4FC3FF);
        polygon(c, new double[]{cx - r, cx, cx, cx - r}, new double[]{y + h, y + r, y + 2 * r, y + 1.5 * r}, 0xFF1565C0);
        polygon(c, new double[]{cx, cx + r, cx + r, cx}, new double[]{y + r, y + h, y + 1.5 * r, y + 2 * r}, 0xFF0D47A1);
    }

    public static void ring(DrawContext c, int cx, int cy, int rx, int ry, int color) {
        for (int a = 0; a < 360; a += 3) {
            double rad = Math.toRadians(a);
            int px = cx + (int) Math.round(Math.cos(rad) * rx);
            int py = cy + (int) Math.round(Math.sin(rad) * ry);
            c.fill(px, py, px + 1, py + 1, color);
        }
    }

    public static void icon(DrawContext c, int index, int x, int y, int color) {
        if (index < 0 || index >= ICONS.length) return;
        String[] rows = ICONS[index];
        for (int r = 0; r < rows.length; r++) {
            for (int col = 0; col < rows[r].length(); col++) {
                if (rows[r].charAt(col) == '#') {
                    c.fill(x + col, y + r, x + col + 1, y + r + 1, color);
                }
            }
        }
    }

    public static void playTriangle(DrawContext c, int x, int y, int size, int color) {
        polygon(c, new double[]{x, x + size, x}, new double[]{y, y + size / 2.0, y + size}, color);
    }

    public static List<String> wrap(TextRenderer tr, String s, int maxW, int maxLines) {
        List<String> out = new ArrayList<>();
        if (s == null) return out;
        StringBuilder line = new StringBuilder();
        for (String word : s.replace('\n', ' ').split(" ")) {
            if (word.isEmpty()) continue;
            String test = line.length() == 0 ? word : line + " " + word;
            if (tr.getWidth(test) > maxW && line.length() > 0) {
                out.add(line.toString());
                line = new StringBuilder(word);
                if (out.size() >= maxLines) return out;
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0 && out.size() < maxLines) out.add(line.toString());
        return out;
    }
}
