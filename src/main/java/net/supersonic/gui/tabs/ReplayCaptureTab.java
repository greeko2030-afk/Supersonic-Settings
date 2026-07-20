package net.supersonic.gui.tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.supersonic.gui.widgets.SupersonicDropdown;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicToggle;
import java.util.List;

public class ReplayCaptureTab {
    private final int x, y;

    public ReplayCaptureTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets, MinecraftClient client) {
        int contentX = x + 200;
        int widgetX = contentX + 380;
        int currentY = y + 70;

        // Replay Mod Enable/Disable Toggle
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, true, state -> {
            // Hook into ReplayMod config API here
            // e.g., ReplayMod.getConfig().setRecordingEnabled(state);
        }));
        currentY += 40;

        // Replay Buffer Duration Dropdown
        widgets.add(new SupersonicDropdown(widgetX - 40, currentY, 100, 16, Text.literal("5 Minutes"), btn -> {
            btn.setMessage(Text.literal("10 Minutes")); // Cycle logic
        }));
        currentY += 40;

        // Camera Speed Slider (For Replay Viewer)
        currentY += 150; // Skip to Camera Controls
        widgets.add(new SupersonicSlider(widgetX - 60, currentY, 120, 16, Text.empty(), 0.5, "", "1.25x") {
            @Override
            protected void applyValue() {
                // Hook into Replay Camera API
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(String.format("%.2fx", 0.5 + (this.value * 2.0))));
            }
        });
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int contentX = x + 200;

        context.drawText(client.textRenderer, "REPLAY & CAPTURE", contentX, y + 20, 0xFFFFFF, true);
        
        int currentY = y + 75;
        context.drawText(client.textRenderer, "Enable Replay Mod", contentX, currentY, 0xFFFFFF, false);
        currentY += 40;
        context.drawText(client.textRenderer, "Replay Buffer Duration", contentX, currentY, 0xFFFFFF, false);
        currentY += 40;
        context.drawText(client.textRenderer, "Max Memory Usage", contentX, currentY, 0xFFFFFF, false);
        
        currentY += 110;
        context.drawText(client.textRenderer, "CAMERA CONTROLS", contentX, currentY, 0x00FFFF, false);
        currentY += 40;
        context.drawText(client.textRenderer, "Camera Speed", contentX, currentY, 0xFFFFFF, false);
        
        // Draw Replay Preview Right Panel
        int panelX = x + 720;
        int panelY = y + 20;
        context.fill(panelX, panelY, panelX + 250, panelY + 180, 0xFF0A111A);
        context.drawBorder(panelX, panelY, 250, 180, 0xFF1A3344);
        context.drawText(client.textRenderer, "REPLAY PREVIEW", panelX + 10, panelY + 10, 0x00FFFF, false);
    }
}
