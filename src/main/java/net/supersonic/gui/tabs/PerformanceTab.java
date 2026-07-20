package net.supersonic.gui.tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicToggle;
import java.util.List;

public class PerformanceTab {
    private final int x, y;

    public PerformanceTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets, MinecraftClient client) {
        int contentX = x + 200;
        int widgetX = contentX + 380;
        int startY = y + 70;

        // --- REAL Max FPS Slider (10 to 260) ---
        int currentFps = client.options.getMaxFps().getValue();
        double fpsProgress = (currentFps == 260) ? 1.0 : (currentFps - 10.0) / 250.0;
        
        widgets.add(new SupersonicSlider(widgetX, startY, 120, 16, Text.empty(), fpsProgress, "", (currentFps == 260 ? "Unlimited" : currentFps + " FPS")) {
            @Override
            protected void applyValue() {
                int newFps = (int) (10 + (this.value * 250));
                if (newFps >= 255) newFps = 260; // 260 is considered unlimited in vanilla
                client.options.getMaxFps().setValue(newFps);
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                int val = client.options.getMaxFps().getValue();
                this.setMessage(Text.literal(val >= 260 ? "Unlimited" : val + " FPS"));
            }
        });

        // --- REAL Render Distance Slider (2 to 32) ---
        startY += 160; // Jump to Render Optimization section
        int currentRenderDist = client.options.getViewDistance().getValue();
        double renderProgress = (currentRenderDist - 2.0) / 30.0;

        widgets.add(new SupersonicSlider(widgetX, startY, 120, 16, Text.empty(), renderProgress, "", currentRenderDist + " Chunks") {
            @Override
            protected void applyValue() {
                int chunks = (int) (2 + (this.value * 30));
                client.options.getViewDistance().setValue(chunks);
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(client.options.getViewDistance().getValue() + " Chunks"));
            }
        });

        // --- REAL Simulation Distance Slider ---
        startY += 30;
        int currentSimDist = client.options.getSimulationDistance().getValue();
        double simProgress = (currentSimDist - 5.0) / 27.0; // Usually 5 to 32

        widgets.add(new SupersonicSlider(widgetX, startY, 120, 16, Text.empty(), simProgress, "", currentSimDist + " Chunks") {
            @Override
            protected void applyValue() {
                int chunks = (int) (5 + (this.value * 27));
                client.options.getSimulationDistance().setValue(chunks);
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(client.options.getSimulationDistance().getValue() + " Chunks"));
            }
        });
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int contentX = x + 200;
        context.drawText(MinecraftClient.getInstance().textRenderer, "PERFORMANCE", contentX, y + 20, 0xFFFFFF, true);
        
        // Draw Performance Overview Right Panel (Real FPS reading)
        int panelX = x + 720;
        int panelY = y + 20;
        context.fill(panelX, panelY, panelX + 200, panelY + 480, 0xFF0A111A);
        context.drawBorder(panelX, panelY, 200, 480, 0xFF1A3344);
        
        context.drawText(MinecraftClient.getInstance().textRenderer, "PERFORMANCE OVERVIEW", panelX + 20, panelY + 15, 0x00FFFF, false);
        
        // Real Live FPS
        String liveFps = MinecraftClient.getInstance().getCurrentFps() + " FPS";
        context.getMatrices().push();
        context.getMatrices().scale(1.5f, 1.5f, 1.5f);
        context.drawText(MinecraftClient.getInstance().textRenderer, liveFps, (int)((panelX + 60) / 1.5f), (int)((panelY + 100) / 1.5f), 0xFFFFFF, true);
        context.getMatrices().pop();
    }
}
