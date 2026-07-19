package net.supersonic.gui.tabs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class GameplayTab {
    private final int x;
    private final int y;

    public GameplayTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets) {
        int leftColX = x + 180; 
        int settingSpacing = 32;
        int btnX = leftColX + 330;
        
        // --- GENERAL SECTION ---
        int currentY = y + 85;
        widgets.add(new SupersonicDropdown(btnX, currentY - 4, 120, 18, Text.literal("Crosshair"), btn -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(btnX + 80, currentY, 40, 18, true, state -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(btnX + 80, currentY, 40, 18, true, state -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(btnX + 80, currentY, 40, 18, true, state -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(btnX + 80, currentY, 40, 18, true, state -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(btnX + 80, currentY, 40, 18, true, state -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(btnX - 20, currentY, 140, 18, Text.empty(), 0.3, "", " Ticks"));

        // --- COMBAT SECTION ---
        currentY += 45;
        widgets.add(new SupersonicDropdown(btnX, currentY - 4, 120, 18, Text.literal("Particles"), btn -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(btnX + 80, currentY, 40, 18, true, state -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicDropdown(btnX, currentY - 4, 120, 18, Text.literal("Dynamic"), btn -> {}));

        // --- WORLD SECTION ---
        currentY += 45;
        widgets.add(new SupersonicToggle(btnX + 80, currentY, 40, 18, true, state -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(btnX + 80, currentY, 40, 18, true, state -> {}));
        
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(btnX + 80, currentY, 40, 18, true, state -> {}));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int leftColX = x + 180;
        int rightColX = x + 660; // Adjusted for the right side panel
        int settingSpacing = 32;
        
        // --- HEADER ---
        context.drawText(context.textRenderer, "🎮 GAMEPLAY", leftColX, y + 30, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Customize your in-game experience.", leftColX, y + 45, 0xAAAAAA, false);
        
        // --- SETTINGS LIST ---
        int currentY = y + 70;
        context.drawText(context.textRenderer, "GENERAL", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSetting(context, leftColX, currentY, "Attack Indicator", "Show attack cooldown indicator.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Auto Jump", "Automatically jump when moving forward.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Sprint Toggle", "Toggle sprint on key press.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Sneak Toggle", "Toggle sneak on key press.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Smart Sprint", "Automatically sprint in optimal conditions.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Auto Climb", "Automatically climb ladders and vines.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Item Pickup Delay", "Delay before picking up items.");

        currentY += 30;
        context.drawText(context.textRenderer, "COMBAT", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSetting(context, leftColX, currentY, "Criticals", "Enable critical hit particles.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Shield Indicator", "Show shield durability above hotbar.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Weapon Swing Style", "Change the swing animation style.");

        currentY += 30;
        context.drawText(context.textRenderer, "WORLD", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSetting(context, leftColX, currentY, "Block Outline", "Highlight targeted block.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Show Coordinates", "Show coordinates in the debug screen.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Enhanced Interaction", "Smoother block & entity interaction.");

        // --- RIGHT PANEL (SUPERSONIC ENGINE) ---
        int panelY = y + 70;
        int panelWidth = 200;
        int panelHeight = 420;
        
        // Draw Panel Background and Border
        context.fill(rightColX, panelY, rightColX + panelWidth, panelY + panelHeight, 0xFF050A10);
        context.drawBorder(rightColX, panelY, panelWidth, panelHeight, 0xFF113344);
        
        // Title
        context.drawText(context.textRenderer, "SUPERSONIC ENGINE", rightColX + 45, panelY + 15, 0xFFFFFF, true);
        
        // Mockup for the 3D Cube Graphic (Using a colored rectangle for placement)
        context.fill(rightColX + 50, panelY + 50, rightColX + 150, panelY + 150, 0xFF003355);
        context.drawBorder(rightColX + 50, panelY + 50, 100, 100, 0xFF00FFFF);

        int statY = panelY + 180;
        
        // RENDERER
        context.drawText(context.textRenderer, "RENDERER", rightColX + 15, statY, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "Supersonic Engine", rightColX + 15, statY + 12, 0x00FFFF, false);
        
        // GRAPHICS API
        statY += 35;
        context.drawText(context.textRenderer, "GRAPHICS API", rightColX + 15, statY, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "Direct3D 12", rightColX + 15, statY + 12, 0x00FFFF, false);
        
        // FPS
        statY += 35;
        context.drawText(context.textRenderer, "FPS (AVERAGE)", rightColX + 15, statY, 0xAAAAAA, false);
        context.drawTextWithShadow(context.textRenderer, "4000+ FPS", rightColX + 15, statY + 15, 0x00FFFF); // Use larger font in actual mod
        
        // GPU TEMP
        statY += 45;
        context.drawText(context.textRenderer, "GPU TEMP", rightColX + 15, statY, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "40°C", rightColX + 15, statY + 12, 0x00FF00, false);
        
        // ENGINE STATUS
        statY += 35;
        context.drawText(context.textRenderer, "ENGINE STATUS", rightColX + 15, statY, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "Optimal", rightColX + 15, statY + 12, 0x00FF00, false);
    }

    private void drawSetting(DrawContext context, int x, int y, String title, String desc) {
        // Adds an icon placeholder spacing (assuming 16x16 icon)
        int textX = x + 25; 
        context.drawText(context.textRenderer, title, textX, y, 0xFFFFFF, false);
        context.drawText(context.textRenderer, desc, textX, y + 12, 0x888888, false);
        
        // Placeholder for the small icons next to setting names
        context.fill(x, y + 2, x + 12, y + 14, 0xFF335577);
    }
}
