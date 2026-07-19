package net.supersonic.gui.tabs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class VisualsTab {
    private final int x;
    private final int y;

    public VisualsTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets) {
        int leftColX = x + 180;
        int settingControlX = leftColX + 330; 
        
        // ================= GRAPHICS QUALITY SETTINGS =================
        int currentY = y + 70;
        
        // Graphics Quality
        widgets.add(new SupersonicDropdown(settingControlX - 70, currentY, 80, 16, Text.literal("Custom"), btn -> {}));
        currentY += 32;
        
        // Smooth Lighting
        widgets.add(new SupersonicToggle(settingControlX - 30, currentY, 40, 16, true, state -> {}));
        currentY += 32;
        
        // Entity Shadows
        widgets.add(new SupersonicToggle(settingControlX - 30, currentY, 40, 16, true, state -> {}));
        currentY += 32;
        
        // Block Shadows
        widgets.add(new SupersonicToggle(settingControlX - 30, currentY, 40, 16, true, state -> {}));
        currentY += 32;
        
        // Cloud Quality
        widgets.add(new SupersonicDropdown(settingControlX - 70, currentY, 80, 16, Text.literal("High"), btn -> {}));
        currentY += 32;
        
        // Weather Quality
        widgets.add(new SupersonicDropdown(settingControlX - 70, currentY, 80, 16, Text.literal("High"), btn -> {}));
        currentY += 32;
        
        // Colors
        widgets.add(new SupersonicDropdown(settingControlX - 70, currentY, 80, 16, Text.literal("Vibrant"), btn -> {}));
        
        // ================= ADVANCED SETTINGS =================
        currentY += 55;
        
        // Render Distance
        widgets.add(new SupersonicSlider(settingControlX - 160, currentY, 110, 16, Text.empty(), 0.75, "", "24 Chunks"));
        currentY += 32;
        
        // Simulation Distance
        widgets.add(new SupersonicSlider(settingControlX - 160, currentY, 110, 16, Text.empty(), 0.5, "", "16 Chunks"));
        currentY += 32;
        
        // Brightness
        widgets.add(new SupersonicSlider(settingControlX - 160, currentY, 110, 16, Text.empty(), 1.0, "", "100%"));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int leftColX = x + 180;
        int rightColX = x + 540;
        
        // --- HEADER ---
        context.drawText(context.textRenderer, "VISUALS", leftColX + 25, y + 20, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Optimize. Accelerate. Dominate.", leftColX + 25, y + 35, 0xAAAAAA, false);
        context.fill(leftColX, y + 20, leftColX + 16, y + 36, 0xFF00FFFF); // Header Icon Placeholder

        // --- LEFT COLUMN (SETTINGS LIST) ---
        int currentY = y + 55;
        
        context.drawText(context.textRenderer, "GRAPHICS QUALITY", leftColX, currentY, 0x00FFFF, false);
        currentY += 15;
        
        drawSetting(context, leftColX, currentY, "Graphics Quality", "Overall graphics quality level.", 0xFF55AAFF);
        currentY += 32;
        drawSetting(context, leftColX, currentY, "Smooth Lighting", "Enable smooth lighting for better visuals.", 0xFF55AAFF);
        currentY += 32;
        drawSetting(context, leftColX, currentY, "Entity Shadows", "Render shadows for entities.", 0xFF55AAFF);
        currentY += 32;
        drawSetting(context, leftColX, currentY, "Block Shadows", "Render shadows for blocks.", 0xFF55AAFF);
        currentY += 32;
        drawSetting(context, leftColX, currentY, "Cloud Quality", "Adjust cloud rendering quality.", 0xFF55AAFF);
        currentY += 32;
        drawSetting(context, leftColX, currentY, "Weather Quality", "Adjust rain, snow and thunder quality.", 0xFF55AAFF);
        currentY += 32;
        drawSetting(context, leftColX, currentY, "Colors", "Adjust world colors and saturation.", 0xFF55AAFF);

        currentY += 40;
        context.drawText(context.textRenderer, "ADVANCED", leftColX, currentY, 0x00FFFF, false);
        currentY += 15;
        
        drawSetting(context, leftColX, currentY, "Render Distance", "Adjust how far you can see.", 0xFF55AAFF);
        currentY += 32;
        drawSetting(context, leftColX, currentY, "Simulation Distance", "Adjust simulation distance.", 0xFF55AAFF);
        currentY += 32;
        drawSetting(context, leftColX, currentY, "Brightness", "Adjust screen brightness.", 0xFF55AAFF);
        
        // --- RIGHT PANEL (SUPERSONIC ENGINE) ---
        int panelWidth = 160;
        int panelHeight = 350;
        DrawEnginePanel(context, rightColX, y + 55, panelWidth, panelHeight);
    }

    private void drawSetting(DrawContext context, int x, int y, String title, String desc, int iconColor) {
        // Setting Icon Placeholder
        context.fill(x, y + 2, x + 12, y + 14, iconColor);
        
        context.drawText(context.textRenderer, title, x + 20, y, 0xFFFFFF, false);
        context.drawText(context.textRenderer, desc, x + 20, y + 10, 0x777777, false);
        
        // Subtle Separator Line
        context.fill(x, y + 25, x + 350, y + 26, 0x33FFFFFF);
    }

    private void DrawEnginePanel(DrawContext context, int x, int y, int width, int height) {
        // Background and Border
        context.fill(x, y, x + width, y + height, 0xFF0A111A);
        context.drawBorder(x, y, width, height, 0xFF1A3344);
        
        // Panel Title
        int titleWidth = context.textRenderer.getWidth("SUPERSONIC ENGINE");
        context.drawText(context.textRenderer, "SUPERSONIC ENGINE", x + (width - titleWidth) / 2, y + 15, 0xFFFFFF, false);
        
        // 3D Cube Graphic Placeholder
        int graphicY = y + 40;
        context.fill(x + 30, graphicY, x + width - 30, graphicY + 80, 0xFF0055AA);
        context.drawBorder(x + 30, graphicY, width - 60, 80, 0xFF00FFFF);
        
        // Engine Stats
        int statsY = graphicY + 100;
        
        // Renderer
        context.drawText(context.textRenderer, "RENDERER", x + 10, statsY, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "Supersonic Engine", x + 10, statsY + 10, 0x00FFFF, false);
        
        // Graphics API
        statsY += 30;
        context.drawText(context.textRenderer, "GRAPHICS API", x + 10, statsY, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "Direct3D 12", x + 10, statsY + 10, 0x00FFFF, false);
        
        // FPS
        statsY += 35;
        context.drawText(context.textRenderer, "FPS (AVERAGE)", x + 10, statsY, 0xAAAAAA, false);
        context.getMatrices().push();
        context.getMatrices().scale(1.5f, 1.5f, 1.5f);
        context.drawText(context.textRenderer, "4000+ FPS", (int)((x + 10) / 1.5f), (int)((statsY + 10) / 1.5f), 0x00FFFF, true);
        context.getMatrices().pop();
        
        // GPU Temp
        statsY += 45;
        context.drawText(context.textRenderer, "GPU TEMP", x + 10, statsY, 0xAAAAAA, false);
        context.getMatrices().push();
        context.getMatrices().scale(1.5f, 1.5f, 1.5f);
        context.drawText(context.textRenderer, "40°C", (int)((x + 10) / 1.5f), (int)((statsY + 10) / 1.5f), 0x55FF55, true); // Green Color
        context.getMatrices().pop();
    }
}
