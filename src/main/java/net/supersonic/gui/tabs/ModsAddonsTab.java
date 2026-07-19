package net.supersonic.gui.tabs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class ModsAddonsTab {
    private final int x;
    private final int y;

    public ModsAddonsTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets) {
        int leftColX = x + 180; 
        int rightColX = x + 530;
        int listWidth = 320;
        int toggleX = leftColX + 270;
        
        // ================= LEFT COLUMN FILTERS =================
        int filterY = y + 75;
        widgets.add(new SupersonicDropdown(leftColX, filterY, 100, 18, Text.literal("All (23)"), btn -> {}));
        widgets.add(new SupersonicDropdown(leftColX + 110, filterY, 100, 18, Text.literal("Sort by: Name"), btn -> {}));
        widgets.add(ButtonWidget.builder(Text.literal("Search mods..."), btn -> {}).dimensions(leftColX + 220, filterY, 110, 18).build());

        // ================= LEFT COLUMN MOD LIST (TOGGLES & BUTTONS) =================
        int currentY = y + 120;
        // Active Mods (Mockup)
        for (int i = 0; i < 4; i++) {
            widgets.add(new SupersonicToggle(toggleX, currentY, 35, 14, true, state -> {}));
            widgets.add(ButtonWidget.builder(Text.literal("⋮"), btn -> {}).dimensions(toggleX + 40, currentY - 1, 14, 16).build());
            currentY += 28;
        }
        
        currentY += 25; // Skip to Inactive
        // Inactive Mods (Mockup)
        for (int i = 0; i < 2; i++) {
            widgets.add(new SupersonicToggle(toggleX, currentY, 35, 14, false, state -> {}));
            widgets.add(ButtonWidget.builder(Text.literal("⋮"), btn -> {}).dimensions(toggleX + 40, currentY - 1, 14, 16).build());
            currentY += 28;
        }

        // Bottom Action Buttons
        int bottomActionY = y + 420;
        widgets.add(ButtonWidget.builder(Text.literal("Open Mods Folder"), btn -> {}).dimensions(leftColX, bottomActionY, 105, 24).build());
        widgets.add(ButtonWidget.builder(Text.literal("Update All"), btn -> {}).dimensions(leftColX + 112, bottomActionY, 105, 24).build());
        widgets.add(ButtonWidget.builder(Text.literal("Check Compatibility"), btn -> {}).dimensions(leftColX + 225, bottomActionY, 105, 24).build());


        // ================= RIGHT COLUMN PANELS =================
        int rightPanelWidth = 240;

        // --- MOD DETAILS ---
        int detailsY = y + 55;
        widgets.add(ButtonWidget.builder(Text.literal("Website"), btn -> {}).dimensions(rightColX + 10, detailsY + 80, 105, 18).build());
        widgets.add(ButtonWidget.builder(Text.literal("Issues"), btn -> {}).dimensions(rightColX + 125, detailsY + 80, 105, 18).build());
        widgets.add(ButtonWidget.builder(Text.literal("Disable"), btn -> {}).dimensions(rightColX + 10, detailsY + 102, 105, 18).build());
        widgets.add(ButtonWidget.builder(Text.literal("Remove"), btn -> {}).dimensions(rightColX + 125, detailsY + 102, 105, 18).build());

        // --- ADDON BROWSER ---
        int addonY = y + 195;
        widgets.add(new SupersonicDropdown(rightColX + 150, addonY + 5, 60, 14, Text.literal("Featured"), btn -> {}));
        widgets.add(ButtonWidget.builder(Text.literal("Install"), btn -> {}).dimensions(rightColX + 190, addonY + 28, 40, 14).build());
        widgets.add(ButtonWidget.builder(Text.literal("Install"), btn -> {}).dimensions(rightColX + 190, addonY + 50, 40, 14).build());
        widgets.add(ButtonWidget.builder(Text.literal("Install"), btn -> {}).dimensions(rightColX + 190, addonY + 72, 40, 14).build());
        widgets.add(ButtonWidget.builder(Text.literal("Browse More on Modrinth"), btn -> {}).dimensions(rightColX + 10, addonY + 95, 220, 18).build());

        // --- MOD PACKS ---
        int packY = y + 330;
        widgets.add(ButtonWidget.builder(Text.literal("Install"), btn -> {}).dimensions(rightColX + 190, packY + 50, 40, 14).build());
        widgets.add(ButtonWidget.builder(Text.literal("Manage Modpacks"), btn -> {}).dimensions(rightColX + 10, packY + 75, 220, 18).build());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int leftColX = x + 180;
        int rightColX = x + 530;
        int rightPanelWidth = 240;
        
        // --- HEADER ---
        context.drawText(context.textRenderer, "MODS & ADDONS", leftColX + 25, y + 20, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Manage your mods, resource packs and addons.", leftColX + 25, y + 35, 0xAAAAAA, false);
        
        // Category Icon Placeholder
        context.fill(leftColX, y + 20, leftColX + 16, y + 36, 0xFF00FFFF);

        // ================= TOP SUB-TABS RENDER =================
        int subTabY = y + 55;
        context.drawText(context.textRenderer, "MODS", leftColX + 10, subTabY, 0xFFFFFF, false);
        context.drawText(context.textRenderer, "RESOURCE PACKS", leftColX + 70, subTabY, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "SHADERS", leftColX + 180, subTabY, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "ADDONS", leftColX + 250, subTabY, 0xAAAAAA, false);
        
        // Active Tab Underline
        context.fill(leftColX + 5, subTabY + 12, leftColX + 45, subTabY + 14, 0xFF00FFFF);

        // ================= LEFT COLUMN MOD LIST =================
        int currentY = y + 105;
        context.drawText(context.textRenderer, "ACTIVE MODS (16)", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawModEntry(context, leftColX, currentY, "Sodium", "The fastest and most compatible rendering optimization mod.", "v0.5.8", 0xFF44FF44);
        currentY += 28;
        drawModEntry(context, leftColX, currentY, "Iris", "A modern shaders mod for Minecraft.", "v1.6.14", 0xFFFF5555);
        currentY += 28;
        drawModEntry(context, leftColX, currentY, "Lithium", "General performance and server optimization.", "v0.11.2", 0xFF5555FF);
        currentY += 28;
        drawModEntry(context, leftColX, currentY, "FerriteCore", "Reduces memory usage.", "v6.0.1", 0xFFAAAA55);

        currentY += 35;
        context.drawText(context.textRenderer, "INACTIVE MODS (7)", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawModEntry(context, leftColX, currentY, "Cloth Config API", "Configuration Library for Minecraft Mods.", "v11.1.118", 0xFF888888);
        currentY += 28;
        drawModEntry(context, leftColX, currentY, "Replay Mod", "Record, playback and share your Minecraft.", "v2.6.14", 0xFF55AAFF);


        // ================= RIGHT COLUMN PANELS =================

        // --- MOD DETAILS ---
        DrawPanelBox(context, rightColX, y + 55, rightPanelWidth, 130, "MOD DETAILS");
        int detailsY = y + 75;
        context.fill(rightColX + 10, detailsY, rightColX + 40, detailsY + 30, 0xFF44FF44); // Big Icon
        context.drawText(context.textRenderer, "Sodium", rightColX + 50, detailsY, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "v0.5.8 • fabric", rightColX + 50, detailsY + 12, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "By JellySquid", rightColX + 50, detailsY + 22, 0x888888, false);
        // Active Badge
        context.fill(rightColX + 195, detailsY, rightColX + 230, detailsY + 12, 0xFF225522);
        context.drawText(context.textRenderer, "ACTIVE", rightColX + 200, detailsY + 2, 0xFF55FF55, false);
        // Description
        context.drawText(context.textRenderer, "The fastest and most compatible", rightColX + 10, detailsY + 40, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "rendering optimization mod.", rightColX + 10, detailsY + 50, 0xCCCCCC, false);

        // --- ADDON BROWSER ---
        DrawPanelBox(context, rightColX, y + 195, rightPanelWidth, 125, "ADDON BROWSER");
        int addonY = y + 225;
        drawAddonEntry(context, rightColX + 10, addonY, "Vanish Water (Shader)", "Immersive water shaders and effects.", 0xFF00AAFF);
        drawAddonEntry(context, rightColX + 10, addonY + 22, "AppleSkin", "Adds food/hunger info in tooltip.", 0xFFFFAA00);
        drawAddonEntry(context, rightColX + 10, addonY + 44, "Mod Menu", "Adds a mod menu to view mods.", 0xFF5555FF);

        // --- MOD PACKS ---
        DrawPanelBox(context, rightColX, y + 330, rightPanelWidth, 105, "MOD PACKS");
        int packY = y + 355;
        drawAddonEntry(context, rightColX + 10, packY, "Supersonic Optimized", "The best FPS + Visuals balance.", 0xFFFF5555);
        context.drawText(context.textRenderer, "INSTALLED", rightColX + 180, packY + 2, 0xFF55FF55, false); // Installed text instead of button
        
        drawAddonEntry(context, rightColX + 10, packY + 25, "NarratorMC Essentials", "Official server optimization pack.", 0xFF00FFFF);
    }

    private void drawModEntry(DrawContext context, int x, int y, String name, String desc, String version, int color) {
        // Mod Icon Placeholder
        context.fill(x, y, x + 16, y + 16, color);
        
        context.drawText(context.textRenderer, name, x + 25, y, 0xFFFFFF, false);
        context.drawText(context.textRenderer, desc, x + 25, y + 10, 0x777777, false);
        context.drawText(context.textRenderer, version, x + 230, y + 4, 0xAAAAAA, false);
    }

    private void drawAddonEntry(DrawContext context, int x, int y, String title, String desc, int color) {
        context.fill(x, y, x + 12, y + 12, color); // Icon
        context.drawText(context.textRenderer, title, x + 18, y - 2, 0xFFFFFF, false);
        context.drawText(context.textRenderer, desc, x + 18, y + 7, 0x666666, false);
    }

    private void DrawPanelBox(DrawContext context, int x, int y, int width, int height, String title) {
        context.fill(x, y, x + width, y + height, 0xFF0A111A);
        context.drawBorder(x, y, width, height, 0xFF1A3344);
        context.drawText(context.textRenderer, title, x + 10, y + 10, 0x00FFFF, false);
    }
}
