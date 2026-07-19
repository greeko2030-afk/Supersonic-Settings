package net.supersonic.gui.tabs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicSlider;
import java.util.List;

public class PerformanceTab {
    private final int x;
    private final int y;

    public PerformanceTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets) {
        int leftColX = x + 180; // Leaving space for sidebar
        int rightColX = x + 650; // Right side panel
        
        // --- FPS BOOST SECTION ---
        int currentY = y + 100;
        widgets.add(new SupersonicSlider(leftColX + 300, currentY, 150, 20, Text.empty(), 1.0, "", " Unlimited"));
        currentY += 35;
        widgets.add(new SupersonicSlider(leftColX + 300, currentY, 150, 20, Text.empty(), 0.6, "", " FPS"));
        currentY += 35;
        widgets.add(new SupersonicToggle(leftColX + 410, currentY, 40, 18, true, state -> {}));
        currentY += 35;
        widgets.add(new SupersonicToggle(leftColX + 410, currentY, 40, 18, true, state -> {}));
        currentY += 35;
        widgets.add(new SupersonicToggle(leftColX + 410, currentY, 40, 18, true, state -> {}));
        currentY += 35;
        widgets.add(new SupersonicToggle(leftColX + 410, currentY, 40, 18, true, state -> {}));

        // --- RENDER OPTIMIZATION SECTION ---
        currentY += 50;
        widgets.add(new SupersonicSlider(leftColX + 300, currentY, 150, 20, Text.empty(), 0.5, "", " Chunks"));
        currentY += 35;
        widgets.add(new SupersonicSlider(leftColX + 300, currentY, 150, 20, Text.empty(), 0.4, "", " Chunks"));
        currentY += 35;
        widgets.add(new SupersonicToggle(leftColX + 410, currentY, 40, 18, true, state -> {}));
        currentY += 35;
        widgets.add(new SupersonicToggle(leftColX + 410, currentY, 40, 18, true, state -> {}));
        currentY += 35;
        widgets.add(new SupersonicToggle(leftColX + 410, currentY, 40, 18, true, state -> {}));
        currentY += 35;
        widgets.add(new SupersonicToggle(leftColX + 410, currentY, 40, 18, true, state -> {}));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int leftColX = x + 180;
        int rightColX = x + 650;
        
        // Header
        context.drawText(context.textRenderer, "⚡ PERFORMANCE", leftColX, y + 40, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Maximize FPS. Minimize lag. Built for speed.", leftColX, y + 55, 0xAAAAAA, false);
        
        // Section: FPS BOOST
        int currentY = y + 80;
        context.drawText(context.textRenderer, "FPS BOOST", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 25;
        drawSetting(context, leftColX, currentY, "Max FPS", "Set the maximum FPS limit.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "Min FPS", "Set the minimum FPS limit.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "Dynamic FPS", "Automatically adjust FPS based on performance.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "FPS Stabilizer", "Reduce FPS drops and frame spikes.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "AI Frame Interpolation (AIFI)", "Generate extra frames using AI for ultra smooth gameplay.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "Low Latency Mode", "Reduces input delay for faster response.");

        // Section: RENDER OPTIMIZATION
        currentY += 45;
        context.drawText(context.textRenderer, "RENDER OPTIMIZATION", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 25;
        drawSetting(context, leftColX, currentY, "Render Distance", "Adjust how far you can see.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "Simulation Distance", "Adjust how far entities and chunks are simulated.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "Smooth Chunk Loading", "Reduce lag spikes while loading chunks.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "Entity Culling", "Don't render entities you can't see.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "Block & Face Culling", "Reduce unnecessary block rendering.");
        currentY += 35;
        drawSetting(context, leftColX, currentY, "Occlusion Culling", "Don't render what's behind blocks.");

        // --- RIGHT PANEL (PERFORMANCE OVERVIEW) ---
        context.drawText(context.textRenderer, "PERFORMANCE OVERVIEW", rightColX, y + 80, 0x00FFFF, false);
        
        // Mockup for the Circular Gauge
        context.fill(rightColX + 50, y + 120, rightColX + 150, y + 220, 0xFF112233); 
        context.drawText(context.textRenderer, "4000+", rightColX + 80, y + 160, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "FPS", rightColX + 90, y + 175, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "Excellent", rightColX + 75, y + 195, 0x00FF00, false);
        
        // Stats List
        int statY = y + 250;
        drawStat(context, rightColX, statY, "AVG FPS", "4021 FPS");
        drawStat(context, rightColX, statY += 20, "1% LOW", "3856 FPS");
        drawStat(context, rightColX, statY += 20, "FRAME TIME", "0.24 ms");
        drawStat(context, rightColX, statY += 20, "RENDER TIME", "0.19 ms");
        drawStat(context, rightColX, statY += 20, "GPU UTILIZATION", "42%");
        drawStat(context, rightColX, statY += 20, "CPU UTILIZATION", "28%");
        drawStat(context, rightColX, statY += 20, "RAM USAGE", "3.2 / 8 GB");
    }

    private void drawSetting(DrawContext context, int x, int y, String title, String desc) {
        context.drawText(context.textRenderer, title, x + 20, y, 0xFFFFFF, false);
        context.drawText(context.textRenderer, desc, x + 20, y + 12, 0x888888, false);
    }
    
    private void drawStat(DrawContext context, int x, int y, String label, String value) {
        context.drawText(context.textRenderer, label, x, y, 0xCCCCCC, false);
        context.drawText(context.textRenderer, value, x + 150 - context.textRenderer.getWidth(value), y, 0xFFFFFF, false);
    }
}
