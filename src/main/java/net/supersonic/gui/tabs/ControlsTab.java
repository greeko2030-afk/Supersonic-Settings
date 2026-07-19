package net.supersonic.gui.tabs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class ControlsTab {
    private final int x;
    private final int y;

    public ControlsTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets) {
        int leftColX = x + 180; 
        int rightColX = x + 515;
        
        int bindBtnX = leftColX + 220;
        int resetBtnX = bindBtnX + 75;
        int rowSpacing = 20;

        // ================= TOP SUB-TABS & FILTERS =================
        int subTabY = y + 55;
        // Adding hidden standard buttons or custom clickable areas for sub-tabs could go here
        
        int filterY = y + 80;
        // Mockup Search Box (using a button for simplicity in UI, replace with TextFieldWidget in actual logic)
        widgets.add(ButtonWidget.builder(Text.literal("Search keybinds..."), btn -> {}).dimensions(leftColX, filterY, 180, 18).build());
        widgets.add(new SupersonicDropdown(leftColX + 190, filterY, 115, 18, Text.literal("All Categories"), btn -> {}));

        // ================= LEFT COLUMN (KEYBINDS) =================
        
        // --- MOVEMENT ---
        int currentY = y + 125;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "W");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "S");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "A");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "D");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "Space");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "Left Shift");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "Left Ctrl");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "Space");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "C");

        // --- GAMEPLAY ---
        currentY += 35; // Extra gap for category header
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "Left Click");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "Right Click");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "Middle Click");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "Q");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "E");
        currentY += rowSpacing;
        addKeybindRow(widgets, bindBtnX, resetBtnX, currentY, "1");

        // Reset All Button
        currentY += 30;
        widgets.add(ButtonWidget.builder(Text.literal("↺ Reset All Keybinds"), btn -> {}).dimensions(leftColX, currentY, 130, 20).build());

        // ================= RIGHT COLUMN =================
        // Control Profile removed per instructions. Moved other panels up.
        int rightPanelWidth = 265;
        
        // --- QUICK PRESETS ---
        int presetsY = y + 55;
        int presetBtnY = presetsY + 25;
        widgets.add(ButtonWidget.builder(Text.literal("Default"), btn -> {}).dimensions(rightColX + 10, presetBtnY, 245, 18).build());
        widgets.add(ButtonWidget.builder(Text.literal("PvP Optimized"), btn -> {}).dimensions(rightColX + 10, presetBtnY + 22, 245, 18).build());
        widgets.add(ButtonWidget.builder(Text.literal("Building Master"), btn -> {}).dimensions(rightColX + 10, presetBtnY + 44, 245, 18).build());
        widgets.add(ButtonWidget.builder(Text.literal("Minigame Pro"), btn -> {}).dimensions(rightColX + 10, presetBtnY + 66, 245, 18).build());
        widgets.add(ButtonWidget.builder(Text.literal("SpeedRunner"), btn -> {}).dimensions(rightColX + 10, presetBtnY + 88, 245, 18).build());

        // --- KEYBIND OPTIONS ---
        int optionsY = y + 205;
        int optToggleX = rightColX + 215;
        widgets.add(new SupersonicToggle(optToggleX, optionsY + 30, 40, 16, true, state -> {}));
        widgets.add(new SupersonicToggle(optToggleX, optionsY + 60, 40, 16, false, state -> {}));
        widgets.add(new SupersonicToggle(optToggleX, optionsY + 90, 40, 16, true, state -> {}));
        widgets.add(new SupersonicToggle(optToggleX, optionsY + 120, 40, 16, true, state -> {}));
        widgets.add(new SupersonicToggle(optToggleX, optionsY + 150, 40, 16, true, state -> {}));
    }

    private void addKeybindRow(List<ClickableWidget> widgets, int btnX, int resetX, int y, String keyName) {
        widgets.add(ButtonWidget.builder(Text.literal(keyName), btn -> {}).dimensions(btnX, y, 70, 16).build());
        widgets.add(ButtonWidget.builder(Text.literal("↺"), btn -> {}).dimensions(resetX, y, 16, 16).build());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int leftColX = x + 180;
        int rightColX = x + 515;
        int rightPanelWidth = 265;
        int rowSpacing = 20;
        
        // --- HEADER ---
        context.drawText(context.textRenderer, "CONTROLS", leftColX + 30, y + 20, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Customize your keybinds and mouse settings.", leftColX + 30, y + 35, 0xAAAAAA, false);
        
        // Controller Icon Placeholder
        context.fill(leftColX, y + 20, leftColX + 20, y + 36, 0xFF00FFFF);

        // ================= TOP SUB-TABS RENDER =================
        int subTabY = y + 55;
        // Background for sub-tabs
        context.fill(leftColX, subTabY, leftColX + 305, subTabY + 20, 0xFF0A111A);
        context.drawBorder(leftColX, subTabY, 305, 20, 0xFF1A3344);
        
        // Active Tab Highlight (KEYBINDS)
        context.fill(leftColX + 2, subTabY + 2, leftColX + 75, subTabY + 18, 0xFF005577);
        
        // Tab Texts
        context.drawText(context.textRenderer, "KEYBINDS", leftColX + 15, subTabY + 6, 0xFFFFFF, false);
        context.drawText(context.textRenderer, "MOUSE", leftColX + 90, subTabY + 6, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "CONTROLLER", leftColX + 150, subTabY + 6, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "ACCESSIBILITY", leftColX + 225, subTabY + 6, 0xAAAAAA, false);

        // ================= LEFT COLUMN TEXT RENDERING =================
        
        // --- MOVEMENT ---
        int currentY = y + 110;
        context.drawText(context.textRenderer, "MOVEMENT", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 18;
        drawKeybindLabel(context, leftColX, currentY, "Move Forward");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Move Backward");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Move Left");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Move Right");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Jump");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Sneak");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Sprint");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Swim Up");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Swim Down");

        // --- GAMEPLAY ---
        currentY += 35;
        context.drawText(context.textRenderer, "GAMEPLAY", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 18;
        drawKeybindLabel(context, leftColX, currentY, "Attack / Destroy");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Use Item / Place Block");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Pick Block");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Drop Selected Item");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Inventory");
        currentY += rowSpacing;
        drawKeybindLabel(context, leftColX, currentY, "Hotbar Slot 1");

        // ================= RIGHT COLUMN PANELS =================

        // --- QUICK PRESETS ---
        DrawPanelBox(context, rightColX, y + 55, rightPanelWidth, 140, "QUICK PRESETS");
        
        // Active preset checkmark rendering
        int presetCheckY = y + 80;
        context.drawText(context.textRenderer, "✔", rightColX + 240, presetCheckY + 3, 0x00FFFF, false);

        // --- KEYBIND OPTIONS ---
        DrawPanelBox(context, rightColX, y + 205, rightPanelWidth, 180, "KEYBIND OPTIONS");
        int optY = y + 230;
        
        drawOptionLabel(context, rightColX, optY, "Toggle Sprint", "Enable to toggle sprint on/off.");
        optY += 30;
        drawOptionLabel(context, rightColX, optY, "Auto Walk", "Walk forward when no keys are pressed.");
        optY += 30;
        drawOptionLabel(context, rightColX, optY, "Hold to Sneak", "Sneak only while holding the key.");
        optY += 30;
        drawOptionLabel(context, rightColX, optY, "Swap Ctrl / Caps Lock", "Swap the function of Ctrl and Caps Lock.");
        optY += 30;
        drawOptionLabel(context, rightColX, optY, "Show Keybind Hints", "Show keybind hints in-game.");
    }

    private void drawKeybindLabel(DrawContext context, int x, int y, String label) {
        // Icon Placeholder
        context.fill(x, y + 2, x + 12, y + 14, 0xFF224466);
        context.drawText(context.textRenderer, label, x + 20, y + 4, 0xFFFFFF, false);
    }

    private void drawOptionLabel(DrawContext context, int x, int y, String title, String desc) {
        // Icon Placeholder
        context.fill(x + 10, y + 2, x + 22, y + 14, 0xFF224466);
        context.drawText(context.textRenderer, title, x + 30, y, 0xFFFFFF, false);
        context.drawText(context.textRenderer, desc, x + 30, y + 10, 0x777777, false);
    }

    private void DrawPanelBox(DrawContext context, int x, int y, int width, int height, String title) {
        context.fill(x, y, x + width, y + height, 0xFF0A111A);
        context.drawBorder(x, y, width, height, 0xFF1A3344);
        context.drawText(context.textRenderer, title, x + 10, y + 10, 0x00FFFF, false);
    }
}
