package net.supersonic.gui.tabs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class HudOverlayTab {
    private final int x;
    private final int y;

    public HudOverlayTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets) {
        int leftColX = x + 180; 
        int settingSpacing = 26; // Tighter spacing to fit all items
        int leftBtnX = leftColX + 270;
        
        int rightColX = x + 520;
        int rightBtnX = rightColX + 210;

        // ================= LEFT COLUMN =================
        
        // --- HUD ELEMENTS ---
        int currentY = y + 70;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));

        // --- HUD APPEARANCE ---
        currentY += 40;
        widgets.add(new SupersonicSlider(leftBtnX - 80, currentY, 120, 16, Text.empty(), 1.0, "", "100%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicDropdown(leftBtnX - 60, currentY - 2, 100, 16, Text.literal("Blurred"), btn -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicDropdown(leftBtnX - 60, currentY - 2, 100, 16, Text.literal("Supersonic Blue"), btn -> {}));

        // --- OVERLAYS ---
        currentY += 40;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(leftBtnX, currentY, 40, 16, true, state -> {}));


        // ================= RIGHT COLUMN =================
        
        // --- HUD POSITION ---
        int rightY = y + 295;
        widgets.add(new SupersonicDropdown(rightBtnX - 60, rightY - 2, 100, 16, Text.literal("Top Left"), btn -> {}));
        rightY += settingSpacing;
        widgets.add(new SupersonicSlider(rightBtnX - 80, rightY, 120, 16, Text.empty(), 0.1, "", " 10"));
        rightY += settingSpacing;
        widgets.add(new SupersonicSlider(rightBtnX - 80, rightY, 120, 16, Text.empty(), 0.1, "", " 10"));

        // --- HUD INFORMATION ---
        rightY += 40;
        widgets.add(new SupersonicToggle(rightBtnX, rightY, 40, 16, true, state -> {}));
        rightY += settingSpacing;
        widgets.add(new SupersonicToggle(rightBtnX, rightY, 40, 16, true, state -> {}));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int leftColX = x + 180;
        int rightColX = x + 520;
        int settingSpacing = 26;
        
        // --- HEADER ---
        context.drawText(context.textRenderer, "HUD & OVERLAY", leftColX + 25, y + 20, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Customize your in-game HUD and overlays.", leftColX + 25, y + 35, 0xAAAAAA, false);
        
        // Placeholder for the main Header Icon
        context.fill(leftColX, y + 20, leftColX + 16, y + 36, 0xFF00FFFF);

        // ================= LEFT COLUMN TEXT RENDERING =================
        
        int currentY = y + 55;
        context.drawText(context.textRenderer, "HUD ELEMENTS", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSetting(context, leftColX, currentY, "Armor HUD", "Show armor status.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Potion HUD", "Show active potion effects.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Coordinates", "Show your coordinates.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Direction", "Show facing direction.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Clock", "Show in-game time.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "FPS Counter", "Show FPS in-game.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "CPS Counter", "Show clicks per second.");

        currentY += 25;
        context.drawText(context.textRenderer, "HUD APPEARANCE", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSetting(context, leftColX, currentY, "HUD Scale", "Adjust the size of HUD elements.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "HUD Background", "Show background for better visibility.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Background Style", "Choose HUD background style.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "HUD Color Theme", "Choose your HUD color theme.");

        currentY += 25;
        context.drawText(context.textRenderer, "OVERLAYS", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSetting(context, leftColX, currentY, "Boss Bar", "Show boss health bars.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Tab List", "Show player list on TAB.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Scoreboard", "Show scoreboard on the right.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Status Effects Overlay", "Show enlarged status effects.");


        // ================= RIGHT COLUMN RENDERING =================
        
        // --- HUD PREVIEW PANEL ---
        int previewY = y + 55;
        context.drawText(context.textRenderer, "HUD PREVIEW", rightColX, previewY, 0x00FFFF, false);
        
        // Draw the dark background frame for the preview
        context.fill(rightColX, previewY + 15, rightColX + 250, previewY + 215, 0xFF050A10);
        context.drawBorder(rightColX, previewY + 15, 250, 200, 0xFF113344);
        
        // Mockup content inside the preview window
        int previewInnerX = rightColX + 5;
        int previewInnerY = previewY + 20;
        context.drawText(context.textRenderer, "X: 123", previewInnerX, previewInnerY, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Y: 64", previewInnerX, previewInnerY + 10, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Z: -456", previewInnerX, previewInnerY + 20, 0xFFFFFF, true);
        
        context.drawText(context.textRenderer, "NE (-135°)", previewInnerX, previewInnerY + 40, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Biome: Plains", previewInnerX, previewInnerY + 50, 0xFFFFFF, true);
        
        context.drawText(context.textRenderer, "4000+ FPS", rightColX + 190, previewInnerY, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "0 CPS", rightColX + 215, previewInnerY + 15, 0xFFFFFF, true);
        
        // Mock Potion Effects on right side of preview
        context.drawText(context.textRenderer, "Speed II", rightColX + 190, previewInnerY + 45, 0x00FFFF, true);
        context.drawText(context.textRenderer, "Strength II", rightColX + 180, previewInnerY + 70, 0xFF0000, true);

        // --- HUD POSITION ---
        int rightY = y + 280;
        context.drawText(context.textRenderer, "HUD POSITION", rightColX, rightY, 0x00FFFF, false);
        
        rightY += 15;
        drawSetting(context, rightColX, rightY, "HUD Alignment", "Choose HUD alignment on screen.");
        rightY += settingSpacing;
        drawSetting(context, rightColX, rightY, "X Offset", "Horizontal offset.");
        rightY += settingSpacing;
        drawSetting(context, rightColX, rightY, "Y Offset", "Vertical offset.");

        // --- HUD INFORMATION ---
        rightY += 25;
        context.drawText(context.textRenderer, "HUD INFORMATION", rightColX, rightY, 0x00FFFF, false);
        
        rightY += 15;
        drawSetting(context, rightColX, rightY, "Show on Pause Menu", "Keep HUD visible in pause menu.");
        rightY += settingSpacing;
        drawSetting(context, rightColX, rightY, "Show in Spectator", "Show HUD while in spectator mode.");
    }

    private void drawSetting(DrawContext context, int x, int y, String title, String desc) {
        int textX = x + 22; 
        context.drawText(context.textRenderer, title, textX, y, 0xFFFFFF, false);
        
        // Only draw description if we have space (to avoid clutter in tighter spacing)
        context.drawText(context.textRenderer, desc, textX, y + 10, 0x777777, false);
        
        // Icon placeholder
        context.fill(x, y + 2, x + 12, y + 14, 0xFF224466);
    }
}

