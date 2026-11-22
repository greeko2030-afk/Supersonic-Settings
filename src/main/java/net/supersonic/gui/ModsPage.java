package net.supersonic.gui;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.supersonic.gui.Rows.Row;
import net.supersonic.mods.ModrinthBrowser;
import net.supersonic.mods.ModsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Rows of the "Mods & Addons" tab: browse and install mods from Modrinth, switch installed mods on/off,
 * and the list of loaded mods (their details are shown in the right panel).
 */
public final class ModsPage {
    private final SupersonicScreen screen;
    private final List<Row> rows = new ArrayList<>();

    // These rows are created once and reused, so a text box keeps its focus when the list is rebuilt.
    private final Rows.TextInput searchBox;
    private final Rows.Cycle sortCycle;
    private final Rows.Button searchButton;
    private final StatusRow statusRow;

    public ModsPage(SupersonicScreen screen) {
        this.screen = screen;
        this.searchBox = new Rows.TextInput("Search Mods", "Type a mod name, then press Enter or Search.", false,
                () -> ModrinthBrowser.query, v -> ModrinthBrowser.query = v);
        this.searchBox.onSubmit(() -> ModrinthBrowser.search(false));
        this.sortCycle = new Rows.Cycle("Sort By", "How the results are ordered.",
                () -> ModrinthBrowser.sort, v -> {
                    if (v != ModrinthBrowser.sort) {
                        ModrinthBrowser.sort = (ModrinthBrowser.Sort) v;
                        ModrinthBrowser.search(false);
                    }
                },
                () -> ModrinthBrowser.Sort.values(), v -> ((ModrinthBrowser.Sort) v).label);
        this.searchButton = new Rows.Button("Search Modrinth",
                "Fabric mods for Minecraft " + ModrinthBrowser.gameVersion() + " (from modrinth.com).",
                "Search", () -> ModrinthBrowser.search(false));
        this.statusRow = new StatusRow(ModrinthBrowser::statusLine);
        refresh();
    }

    public List<Row> rows() {
        return rows;
    }

    /** Called by the screen when the tab is shown: loads the first page of popular mods once. */
    public void onVisible() {
        ModrinthBrowser.ensureLoaded();
    }

    /** Rebuilds the list. Must run on the render thread (the screen calls it when ModsManager.dirty is set). */
    public void refresh() {
        rows.clear();

        // ---- browse
        rows.add(new Rows.Header("BROWSE MODS (MODRINTH)"));
        rows.add(searchBox);
        rows.add(sortCycle);
        rows.add(searchButton);
        rows.add(statusRow);
        List<ModrinthBrowser.Hit> hits = ModrinthBrowser.results();
        // the installed list is scanned first so that "Installed" labels are correct
        List<ModsManager.Entry> installed = ModsManager.scan();
        for (ModrinthBrowser.Hit h : hits) {
            String desc = formatCount(h.downloads) + " downloads | by " + h.author + " | " + h.description;
            rows.add(new Rows.Button(h.title, desc, () -> ModrinthBrowser.buttonLabel(h), () -> ModrinthBrowser.install(h)));
        }
        if (!hits.isEmpty() && hits.size() < ModrinthBrowser.total()) {
            rows.add(new Rows.Button("More Results", "Load the next 20 mods.", "Load More",
                    () -> ModrinthBrowser.search(true)));
        }

        // ---- installed (jar files in the mods folder)
        rows.add(new Rows.Header("INSTALLED MODS (" + installed.size() + ")"));
        rows.add(new Rows.Info("Restart Needed", "Switching mods on/off or installing mods applies after a game restart.",
                () -> ModsManager.pendingChanges() == 0 ? "No changes" : ModsManager.pendingChanges() + " pending",
                Draw.CYAN, null));
        for (ModsManager.Entry e : installed) {
            String desc = (e.id.isEmpty() ? "" : e.id + " | ") + "v" + e.version + " | " + e.fileName();
            if (e.self) {
                rows.add(new Rows.Info(e.name, "This mod cannot switch itself off.", () -> "Required", Draw.GREEN, null));
                continue;
            }
            rows.add(new Rows.Toggle(e.name, desc, e::enabled, v -> {
                String err = ModsManager.setEnabled(e, v);
                if (err != null) ModrinthBrowser.setStatus(err);
                ModsManager.dirty = true;
            }).noRevert());
        }

        // ---- loaded mods (select one to see its details on the right)
        rows.add(new Rows.Header("LOADED MODS - DETAILS (" + screen.mods.size() + ")"));
        for (int i = 0; i < screen.mods.size(); i++) {
            final int idx = i;
            ModContainer m = screen.mods.get(i);
            String desc = m.getMetadata().getDescription();
            if (desc == null) desc = "";
            int nl = desc.indexOf('\n');
            if (nl >= 0) desc = desc.substring(0, nl);
            rows.add(new Rows.ModRow(m.getMetadata().getName(), desc.trim(),
                    m.getMetadata().getVersion().getFriendlyString(),
                    () -> screen.selectedMod == idx, () -> screen.selectedMod = idx));
        }
    }

    private static String formatCount(long n) {
        if (n >= 1_000_000L) return String.format(Locale.ROOT, "%.1fM", n / 1_000_000.0);
        if (n >= 1_000L) return String.format(Locale.ROOT, "%.1fK", n / 1_000.0);
        return Long.toString(n);
    }

    /** One line of plain text (search / install messages). */
    private static final class StatusRow extends Row {
        private final Supplier<String> text;

        StatusRow(Supplier<String> text) {
            super("", "");
            this.text = text;
        }

        @Override
        public int height() {
            return 16;
        }

        @Override
        public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w, int mx, int my) {
            rx = x;
            ry = y;
            rw = w;
            String s = text.get();
            if (s == null || s.isEmpty()) return;
            Draw.scaled(ctx, tr, tr.trimToWidth(s, (int) ((w - 12) / 0.85f)), x + 6, y + 3, Draw.GRAY, 0.85f);
        }
    }
}
