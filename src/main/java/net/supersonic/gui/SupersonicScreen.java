package net.supersonic.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.supersonic.SupersonicClient;
import net.supersonic.gui.Rows.Row;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SupersonicScreen extends Screen {

    private record Btn(int x, int y, int w, int h, Runnable action) {
    }

    public final List<ModContainer> mods = new ArrayList<>();
    public int selectedMod = 0;

    private final Screen parent;
    private final MinecraftClient mc;
    private final Map<Tab, List<Row>> pages;
    private final List<Btn> buttons = new ArrayList<>();

    private Tab tab = Tab.GAMEPLAY;
    private String searchText = "";
    private List<Row> searchResults = new ArrayList<>();
    private TextFieldWidget search;
    private double scroll = 0;
    private Row dragRow = null;
    private int mouseX;
    private int mouseY;

    // layout
    private int px, py, pw, ph;
    private int sideX, sideY, sideW, itemH;
    private int cx, cw, cyTop, cyBottom;
    private int rightX, rightW;

    public SupersonicScreen(Screen parent) {
        super(Text.literal("Supersonic Settings"));
        this.parent = parent;
        this.mc = MinecraftClient.getInstance();

        List<ModContainer> all = new ArrayList<>(FabricLoader.getInstance().getAllMods());
        all.sort(Comparator.comparing(m -> m.getMetadata().getName().toLowerCase(Locale.ROOT)));
        for (ModContainer m : all) {
            String id = m.getMetadata().getId();
            if (id.startsWith("fabric-") || id.equals("java")) continue; // hide Fabric API sub-modules
            mods.add(m);
        }
        this.pages = Pages.build(mc, this);
    }

    // ------------------------------------------------------------------ layout

    @Override
    protected void init() {
        pw = Math.min(this.width - 16, 780);
        ph = Math.min(this.height - 16, 440);
        px = (this.width - pw) / 2;
        py = (this.height - ph) / 2;

        sideX = px + 8;
        sideY = py + 48;
        sideW = 118;
        itemH = Math.max(16, Math.min(24, (ph - 48 - 70) / Tab.values().length - 2));

        rightW = pw >= 640 ? 170 : 0;
        rightX = px + pw - 8 - rightW;
        cx = sideX + sideW + 10;
        cw = (rightW > 0 ? rightX - 10 : px + pw - 8) - cx;
        cyTop = py + 80;
        cyBottom = py + ph - 34;

        search = new TextFieldWidget(this.textRenderer, px + pw - 214, py + 12, 150, 16, Text.literal("Search"));
        search.setMaxLength(40);
        search.setText(searchText);
        search.setSuggestion(searchText.isEmpty() ? "Search settings..." : "");
        search.setChangedListener(s -> {
            searchText = s;
            search.setSuggestion(s.isEmpty() ? "Search settings..." : "");
            rebuildSearch();
            scroll = 0;
        });
        this.addDrawableChild(search);
    }

    private void rebuildSearch() {
        searchResults = new ArrayList<>();
        String q = searchText.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return;
        for (Tab t : Tab.values()) {
            if (t == Tab.MODS) continue;
            for (Row r : pages.get(t)) {
                if (!r.isHeader() && r.matches(q)) searchResults.add(r);
            }
        }
    }

    private boolean searching() {
        return !searchText.trim().isEmpty();
    }

    private List<Row> currentRows() {
        return searching() ? searchResults : pages.get(tab);
    }

    // --------------------------------------------------------------- buttons

    public void drawButton(DrawContext ctx, int x, int y, int w, int h, String label, boolean primary,
                           int mx, int my, Runnable action) {
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        int bg = primary ? (hover ? 0xFF1A8FC2 : 0xFF0F6FA0) : (hover ? 0xFF17324F : 0xFF0D1E31);
        Draw.round(ctx, x, y, w, h, bg);
        Draw.outline(ctx, x, y, w, h, primary || hover ? Draw.CYAN : Draw.CYAN_DIM);
        if (!label.isEmpty()) {
            Draw.centered(ctx, this.textRenderer, label, x + w / 2, y + (h - 8) / 2, Draw.WHITE);
        }
        buttons.add(new Btn(x, y, w, h, action));
    }

    private void applyAll() {
        mc.options.write();
        SupersonicClient.CONFIG.save();
    }

    private void revertAll() {
        for (List<Row> list : pages.values()) {
            for (Row r : list) r.revert();
        }
    }

    // ------------------------------------------------------------------ render

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Intentionally empty: the background is drawn in render().
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        this.mouseX = mx;
        this.mouseY = my;
        buttons.clear();
        TextRenderer tr = this.textRenderer;

        ctx.fill(0, 0, this.width, this.height, 0x55000000);
        Draw.round(ctx, px, py, pw, ph, Draw.BG);
        Draw.outline(ctx, px, py, pw, ph, Draw.CYAN);
        Draw.outline(ctx, px + 1, py + 1, pw - 2, ph - 2, 0x5500D4FF);

        renderHeader(ctx, tr, mx, my);
        renderSidebar(ctx, tr, mx, my);
        renderContent(ctx, tr, mx, my);

        if (rightW > 0) {
            int ry = py + 48;
            int rh = ph - 48 - 40;
            Panels.render(this, ctx, tr, searching() ? Tab.GAMEPLAY : tab, rightX, ry, rightW, rh, mx, my);
        }

        // Bottom buttons
        int by = py + ph - 28;
        int bw = 64;
        int bx = px + pw - 8 - bw * 3 - 8;
        drawButton(ctx, bx, by, bw, 18, "Apply", true, mx, my, this::applyAll);
        drawButton(ctx, bx + bw + 4, by, bw, 18, "Reset", false, mx, my, this::revertAll);
        drawButton(ctx, bx + (bw + 4) * 2, by, bw, 18, "Done", false, mx, my, this::close);

        renderToasts(ctx, tr);

        super.render(ctx, mx, my, delta); // draws the search box
    }

    private void renderHeader(DrawContext ctx, TextRenderer tr, int mx, int my) {
        Draw.bolt(ctx, px + 12, py + 8, 26, Draw.CYAN);
        Draw.scaledShadow(ctx, tr, "SUPERSONIC SETTINGS", px + 44, py + 7, Draw.CYAN, 1.4f);
        Draw.text(ctx, tr, "Optimize. Accelerate. Dominate.", px + 44, py + 24, Draw.WHITE);
        Draw.rect(ctx, px + 1, py + 42, pw - 2, 1, 0x6600D4FF);

        // Close button
        drawButton(ctx, px + pw - 40, py + 12, 28, 16, "X", false, mx, my, this::close);
    }

    private void renderSidebar(DrawContext ctx, TextRenderer tr, int mx, int my) {
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            int ty = sideY + i * (itemH + 2);
            boolean active = tabs[i] == tab && !searching();
            boolean hover = mx >= sideX && mx < sideX + sideW && my >= ty && my < ty + itemH;
            Draw.round(ctx, sideX, ty, sideW, itemH, active ? 0xFF0F5C86 : (hover ? Draw.ROW_HOVER : Draw.ROW));
            if (active) Draw.outline(ctx, sideX, ty, sideW, itemH, Draw.CYAN);
            Draw.icon(ctx, tabs[i].icon, sideX + 8, ty + (itemH - 8) / 2, active ? Draw.WHITE : Draw.CYAN);
            Draw.text(ctx, tr, tr.trimToWidth(tabs[i].title, sideW - 34), sideX + 22, ty + (itemH - 8) / 2, Draw.WHITE);
        }

        int footerY = py + ph - 50;
        if (footerY > sideY + tabs.length * (itemH + 2) + 4) {
            Draw.round(ctx, sideX, footerY, sideW, 42, Draw.PANEL);
            Draw.outline(ctx, sideX, footerY, sideW, 42, 0xAA0B7FA8);
            Draw.bolt(ctx, sideX + 6, footerY + 8, 18, Draw.CYAN);
            Draw.text(ctx, tr, "Supersonic Engine", sideX + 28, footerY + 6, Draw.WHITE);
            Draw.text(ctx, tr, "Active", sideX + 28, footerY + 17, Draw.GREEN);
            Draw.scaled(ctx, tr, "v1.0.0  |  Ctrl+RShift", sideX + 28, footerY + 29, Draw.GRAY, 0.75f);
        }
    }

    private void renderContent(DrawContext ctx, TextRenderer tr, int mx, int my) {
        // Title
        String title = searching() ? "Search Results (" + searchResults.size() + ")" : tab.title;
        String sub = searching() ? "Settings matching \"" + searchText.trim() + "\"" : tab.subtitle;
        if (!searching()) Draw.icon(ctx, tab.icon, cx, py + 50, Draw.CYAN);
        Draw.scaledShadow(ctx, tr, title.toUpperCase(Locale.ROOT), cx + (searching() ? 0 : 14), py + 48, Draw.WHITE, 1.3f);
        Draw.text(ctx, tr, sub, cx, py + 64, Draw.GRAY);

        List<Row> rows = currentRows();
        int total = 0;
        for (Row r : rows) total += r.height();
        int viewport = cyBottom - cyTop;
        double maxScroll = Math.max(0, total - viewport);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        int rowW = cw - 6;
        int y = cyTop - (int) scroll;
        ctx.enableScissor(cx, cyTop, cx + cw, cyBottom);
        for (Row r : rows) {
            int h = r.height();
            r.rx = cx;
            r.ry = y;
            r.rw = rowW;
            if (y + h >= cyTop && y <= cyBottom) {
                boolean hoverArea = mx >= cx && mx < cx + cw && my >= cyTop && my < cyBottom;
                r.render(ctx, tr, cx, y, rowW, hoverArea ? mx : -1000, hoverArea ? my : -1000);
            }
            y += h;
        }
        ctx.disableScissor();

        if (rows.isEmpty()) {
            Draw.text(ctx, tr, "No settings found.", cx + 6, cyTop + 6, Draw.GRAY);
        }

        // Scroll bar
        if (maxScroll > 0) {
            int barX = cx + cw - 3;
            Draw.rect(ctx, barX, cyTop, 2, viewport, 0x33FFFFFF);
            int knobH = Math.max(16, (int) (viewport * (viewport / (double) total)));
            int knobY = cyTop + (int) ((viewport - knobH) * (scroll / maxScroll));
            Draw.rect(ctx, barX, knobY, 2, knobH, Draw.CYAN);
        }
    }

    private void renderToasts(DrawContext ctx, TextRenderer tr) {
        int space = this.height - (py + ph);
        if (space < 52) return;
        boolean sodium = FabricLoader.getInstance().isModLoaded("sodium");
        int ty = this.height - 46;

        Draw.round(ctx, 8, ty, 170, 40, Draw.PANEL);
        Draw.outline(ctx, 8, ty, 170, 40, 0xAA0B7FA8);
        Draw.bolt(ctx, 14, ty + 8, 20, Draw.CYAN);
        Draw.text(ctx, tr, "Supersonic Engine Active", 40, ty + 5, Draw.CYAN);
        Draw.scaled(ctx, tr, "Renderer: " + (sodium ? "Sodium" : "Vanilla"), 40, ty + 17, Draw.GRAY, 0.8f);
        Draw.scaled(ctx, tr, "Smart Sprint: " + (SupersonicClient.CONFIG.smartSprint ? "ON" : "OFF"), 40, ty + 26, Draw.GRAY, 0.8f);

        int rx = this.width - 148;
        Draw.round(ctx, rx, ty + 8, 140, 32, Draw.PANEL);
        Draw.outline(ctx, rx, ty + 8, 140, 32, 0xAA0B7FA8);
        Draw.bolt(ctx, rx + 6, ty + 14, 18, Draw.CYAN);
        Draw.text(ctx, tr, "Supersonic Client", rx + 30, ty + 14, Draw.CYAN);
        Draw.scaled(ctx, tr, "Ready to dominate.", rx + 30, ty + 26, Draw.GRAY, 0.8f);
    }

    // ------------------------------------------------------------------- input

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true; // search box
        search.setFocused(false);

        if (button == 0) {
            for (Btn b : buttons) {
                if (mx >= b.x && mx < b.x + b.w && my >= b.y && my < b.y + b.h) {
                    b.action.run();
                    return true;
                }
            }
            Tab[] tabs = Tab.values();
            for (int i = 0; i < tabs.length; i++) {
                int ty = sideY + i * (itemH + 2);
                if (mx >= sideX && mx < sideX + sideW && my >= ty && my < ty + itemH) {
                    tab = tabs[i];
                    scroll = 0;
                    if (searching()) {
                        searchText = "";
                        search.setText("");
                    }
                    return true;
                }
            }
        }

        if (mx >= cx && mx < cx + cw && my >= cyTop && my < cyBottom) {
            for (Row r : currentRows()) {
                if (r.click(mx, my, button)) {
                    if (r instanceof Rows.Slider) dragRow = r;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragRow != null && button == 0) {
            dragRow.drag(mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragRow = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount) {
        if (mx >= cx && mx < cx + cw && my >= cyTop && my < cyBottom) {
            scroll -= verticalAmount * 20;
            return true;
        }
        return super.mouseScrolled(mx, my, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        applyAll();
    }

    @Override
    public void close() {
        mc.setScreen(parent);
    }
}
