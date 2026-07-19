package net.supersonic.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.supersonic.gui.tabs.*;
import java.util.ArrayList;
import java.util.List;

public class SupersonicScreen extends Screen {
    private String activeTab = "Visuals"; // Default tab
    private final int startX = 50;
    private final int startY = 50;

    // Tab instances
    private VisualsTab visualsTab;
    private ModsAddonsTab modsAddonsTab;
    private ReplayCaptureTab replayTab;

    public SupersonicScreen() {
        super(Text.literal("Supersonic Settings"));
    }

    @Override
    protected void init() {
        // Initialize widgets based on the active tab
        this.clearChildren();

        if (activeTab.equals("Visuals")) {
            this.visualsTab = new VisualsTab(startX, startY);
            this.visualsTab.init(this.children());
        } else if (activeTab.equals("Mods & Addons")) {
            this.modsAddonsTab = new ModsAddonsTab(startX, startY);
            this.modsAddonsTab.init(this.children());
        } else if (activeTab.equals("Replay & Capture")) {
            this.replayTab = new ReplayCaptureTab(startX, startY);
            this.replayTab.init(this.children());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background
        context.fill(startX, startY, startX + 900, startY + 500, 0xFF050A10);
        context.drawBorder(startX, startY, 900, 500, 0xFF1A3344);

        // Render the left sidebar
        renderSidebar(context);

        // Render the active tab
        if (activeTab.equals("Visuals") && visualsTab != null) visualsTab.render(context, mouseX, mouseY, delta);
        if (activeTab.equals("Mods & Addons") && modsAddonsTab != null) modsAddonsTab.render(context, mouseX, mouseY, delta);
        if (activeTab.equals("Replay & Capture") && replayTab != null) replayTab.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSidebar(DrawContext context) {
        // The left-side buttons and logo will be rendered here
        context.fill(startX, startY, startX + 160, startY + 500, 0xFF0A111A);
        context.drawText(context.textRenderer, "SUPERSONIC", startX + 20, startY + 20, 0x00FFFF, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // When a menu button is clicked, activeTab will change and init() will be called
        // Example: if (isMouseOver("Visuals", mouseX, mouseY)) { activeTab = "Visuals"; init(); }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
                     }
