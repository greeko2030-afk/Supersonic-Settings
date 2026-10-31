package net.supersonic.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Row widgets used by the settings pages: toggle, cycle, slider, button, info. */
public final class Rows {
    private Rows() {
    }

    public static String pretty(Object o) {
        if (o instanceof Enum<?> e) {
            String[] parts = e.name().toLowerCase(Locale.ROOT).split("_");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                if (p.isEmpty()) continue;
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            }
            return sb.toString();
        }
        return String.valueOf(o);
    }

    public abstract static class Row {
        public final String title;
        public final String desc;
        protected int rx;
        protected int ry;
        protected int rw;

        protected Row(String title, String desc) {
            this.title = title;
            this.desc = desc == null ? "" : desc;
        }

        public int height() {
            return 26;
        }

        public boolean isHeader() {
            return false;
        }

        protected int controlWidth() {
            return 100;
        }

        public abstract void render(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my);

        public boolean click(double mx, double my, int button) {
            return false;
        }

        public void drag(double mx) {
        }

        public void capture() {
        }

        public void revert() {
        }

        public boolean matches(String q) {
            return title.toLowerCase(Locale.ROOT).contains(q) || desc.toLowerCase(Locale.ROOT).contains(q);
        }

        protected boolean over(double mx, double my) {
            return mx >= rx && mx < rx + rw && my >= ry && my < ry + height() - 2;
        }

        protected void base(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my) {
            rx = x;
            ry = y;
            rw = w;
            boolean hover = over(mx, my);
            Draw.round(ctx, x, y, w, height() - 2, hover ? Draw.ROW_HOVER : Draw.ROW);
            Draw.rect(ctx, x, y + 3, 2, height() - 8, hover ? Draw.CYAN : Draw.CYAN_DIM);
            Draw.text(ctx, tr, title, x + 8, y + 4, Draw.WHITE);
            if (!desc.isEmpty()) {
                int maxW = (int) ((w - 14 - controlWidth()) / 0.75f);
                Draw.scaled(ctx, tr, tr.trimToWidth(desc, Math.max(20, maxW)), x + 8, y + 15, Draw.GRAY, 0.75f);
            }
        }
    }

    public static final class Header extends Row {
        public Header(String title) {
            super(title, "");
        }

        @Override
        public int height() {
            return 18;
        }

        @Override
        public boolean isHeader() {
            return true;
        }

        @Override
        public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my) {
            rx = x;
            ry = y;
            rw = w;
            Draw.text(ctx, tr, title, x + 2, y + 8, Draw.CYAN);
            Draw.rect(ctx, x + 2 + tr.getWidth(title) + 6, y + 12, Math.max(0, w - tr.getWidth(title) - 12), 1, 0x4400D4FF);
        }
    }

    public static final class Toggle extends Row {
        private final Supplier<Boolean> get;
        private final Consumer<Boolean> set;
        private boolean original;

        public Toggle(String title, String desc, Supplier<Boolean> get, Consumer<Boolean> set) {
            super(title, desc);
            this.get = get;
            this.set = set;
            this.original = get.get();
        }

        @Override
        protected int controlWidth() {
            return 50;
        }

        @Override
        public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my) {
            base(ctx, tr, x, y, w, mx, my);
            boolean on = get.get();
            int px = x + w - 42;
            int py = y + 6;
            Draw.round(ctx, px, py, 34, 12, on ? Draw.CYAN : 0xFF3A4656);
            Draw.rect(ctx, on ? px + 24 : px + 2, py + 2, 8, 8, Draw.WHITE);
            Draw.scaled(ctx, tr, on ? "ON" : "OFF", on ? px + 5 : px + 13, py + 3, on ? 0xFF05202C : Draw.WHITE, 0.7f);
        }

        @Override
        public boolean click(double mx, double my, int button) {
            if (button == 0 && over(mx, my)) {
                set.accept(!get.get());
                return true;
            }
            return false;
        }

        @Override
        public void capture() {
            original = get.get();
        }

        @Override
        public void revert() {
            if (get.get() != original) set.accept(original);
        }
    }

    public static final class Cycle extends Row {
        private final Supplier<Object> get;
        private final Consumer<Object> set;
        private final Supplier<Object[]> values;
        private final Function<Object, String> namer;
        private Object original;

        public Cycle(String title, String desc, Supplier<Object> get, Consumer<Object> set,
                     Supplier<Object[]> values, Function<Object, String> namer) {
            super(title, desc);
            this.get = get;
            this.set = set;
            this.values = values;
            this.namer = namer == null ? Rows::pretty : namer;
            this.original = get.get();
        }

        @Override
        protected int controlWidth() {
            return 110;
        }

        @Override
        public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my) {
            base(ctx, tr, x, y, w, mx, my);
            int bx = x + w - 104;
            int by = y + 4;
            Draw.round(ctx, bx, by, 96, 16, 0xFF0B1826);
            Draw.outline(ctx, bx, by, 96, 16, Draw.CYAN_DIM);
            String label = tr.trimToWidth(namer.apply(get.get()), 74);
            Draw.text(ctx, tr, label, bx + 5, by + 4, Draw.WHITE);
            Draw.text(ctx, tr, "v", bx + 86, by + 4, Draw.CYAN);
        }

        @Override
        public boolean click(double mx, double my, int button) {
            if (!over(mx, my) || (button != 0 && button != 1)) return false;
            Object[] vals = values.get();
            if (vals == null || vals.length == 0) return true;
            Object cur = get.get();
            int idx = 0;
            for (int i = 0; i < vals.length; i++) {
                if (vals[i].equals(cur)) {
                    idx = i;
                    break;
                }
            }
            int next = button == 0 ? idx + 1 : idx - 1 + vals.length;
            set.accept(vals[next % vals.length]);
            return true;
        }

        @Override
        public void capture() {
            original = get.get();
        }

        @Override
        public void revert() {
            if (original != null && !original.equals(get.get())) set.accept(original);
        }
    }

    public static final class Slider extends Row {
        private final double min;
        private final double max;
        private final double step;
        private final Supplier<Double> get;
        private final Consumer<Double> set;
        private final Function<Double, String> fmt;
        private double original;
        private int trackX;
        private int trackW;

        public Slider(String title, String desc, double min, double max, double step,
                      Supplier<Double> get, Consumer<Double> set, Function<Double, String> fmt) {
            super(title, desc);
            this.min = min;
            this.max = max;
            this.step = step;
            this.get = get;
            this.set = set;
            this.fmt = fmt;
            this.original = get.get();
        }

        @Override
        protected int controlWidth() {
            return 175;
        }

        @Override
        public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my) {
            base(ctx, tr, x, y, w, mx, my);
            trackW = 108;
            trackX = x + w - 172;
            int cy = y + 11;
            double v = get.get();
            double f = Math.max(0.0, Math.min(1.0, (v - min) / (max - min)));
            Draw.rect(ctx, trackX, cy, trackW, 2, 0xFF2A3A4C);
            Draw.rect(ctx, trackX, cy, (int) (trackW * f), 2, Draw.CYAN);
            int kx = trackX + (int) (trackW * f) - 2;
            Draw.round(ctx, kx, cy - 4, 5, 10, Draw.WHITE);

            int vx = x + w - 56;
            Draw.round(ctx, vx, y + 4, 48, 16, 0xFF0B1826);
            Draw.outline(ctx, vx, y + 4, 48, 16, Draw.CYAN_DIM);
            String s = tr.trimToWidth(fmt.apply(v), 44);
            Draw.text(ctx, tr, s, vx + 24 - tr.getWidth(s) / 2, y + 8, Draw.WHITE);
        }

        @Override
        public boolean click(double mx, double my, int button) {
            if (button == 0 && over(mx, my) && mx >= trackX - 6 && mx <= trackX + trackW + 6) {
                apply(mx);
                return true;
            }
            return false;
        }

        @Override
        public void drag(double mx) {
            apply(mx);
        }

        private void apply(double mx) {
            double f = Math.max(0.0, Math.min(1.0, (mx - trackX) / (double) trackW));
            double v = min + f * (max - min);
            v = Math.round((v - min) / step) * step + min;
            v = Math.max(min, Math.min(max, v));
            if (Math.abs(v - get.get()) > 1e-9) set.accept(v);
        }

        @Override
        public void capture() {
            original = get.get();
        }

        @Override
        public void revert() {
            if (Math.abs(original - get.get()) > 1e-9) set.accept(original);
        }
    }

    public static final class Button extends Row {
        private final String label;
        private final Runnable action;

        public Button(String title, String desc, String label, Runnable action) {
            super(title, desc);
            this.label = label;
            this.action = action;
        }

        @Override
        public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my) {
            base(ctx, tr, x, y, w, mx, my);
            int bx = x + w - 84;
            Draw.round(ctx, bx, y + 4, 76, 16, 0xFF0F5C86);
            Draw.outline(ctx, bx, y + 4, 76, 16, Draw.CYAN);
            Draw.centered(ctx, tr, label, bx + 38, y + 8, Draw.WHITE);
        }

        @Override
        public boolean click(double mx, double my, int button) {
            if (button == 0 && over(mx, my)) {
                action.run();
                return true;
            }
            return false;
        }
    }

    public static final class Info extends Row {
        private final Supplier<String> value;
        private final int color;
        private final Runnable action;

        public Info(String title, String desc, Supplier<String> value, int color, Runnable action) {
            super(title, desc);
            this.value = value;
            this.color = color;
            this.action = action;
        }

        @Override
        public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my) {
            base(ctx, tr, x, y, w, mx, my);
            String s = tr.trimToWidth(value.get(), 90);
            Draw.round(ctx, x + w - 104, y + 4, 96, 16, 0xFF0B1826);
            Draw.outline(ctx, x + w - 104, y + 4, 96, 16, action != null ? Draw.CYAN_DIM : 0xFF22313F);
            Draw.centered(ctx, tr, s, x + w - 56, y + 8, color);
        }

        @Override
        public boolean click(double mx, double my, int button) {
            if (action != null && button == 0 && over(mx, my)) {
                action.run();
                return true;
            }
            return false;
        }
    }

    public static final class ModRow extends Row {
        private final String version;
        private final Supplier<Boolean> selected;
        private final Runnable onSelect;

        public ModRow(String name, String desc, String version, Supplier<Boolean> selected, Runnable onSelect) {
            super(name, desc);
            this.version = version;
            this.selected = selected;
            this.onSelect = onSelect;
        }

        @Override
        protected int controlWidth() {
            return 70;
        }

        @Override
        public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my) {
            base(ctx, tr, x, y, w, mx, my);
            if (selected.get()) {
                Draw.outline(ctx, x, y, w, height() - 2, Draw.CYAN);
            }
            String v = tr.trimToWidth("v" + version, 64);
            Draw.text(ctx, tr, v, x + w - 8 - tr.getWidth(v), y + 8, Draw.GRAY);
        }

        @Override
        public boolean click(double mx, double my, int button) {
            if (button == 0 && over(mx, my)) {
                onSelect.run();
                return true;
            }
            return false;
        }
    }
}
