package net.supersonic.gui.tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicToggle;
import java.util.List;

public class HudOverlayTab {
    private final int x, y;

    // Config variables for custom HUD elements
    public static boolean showArmorHud = true;
    public static boolean showPotionHud = true;
    public static boolean showFps = true;
    public static boolean showCps = true;

    public HudOverlayTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets, MinecraftClient client) {
        int contentX = x + 200;
        int widgetX = contentX + 380;
        int currentY = y + 70;

        // Custom HUD Element Toggles
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, showArmorHud, state -> showArmorHud = state));
        currentY += 32;
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, showPotionHud, state -> showPotionHud = state));
        currentY += 32;
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, showFps, state -> showFps = state));
        currentY += 32;
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, showCps, state -> showCps = state));
        
        currentY += 60; // Jump to HUD Appearance

        // --- REAL HUD SCALE SLIDER (VANILLA INTEGRATION) ---
        int maxScale = 4; // Minecraft's typical max GUI scale
        int currentScale = client.options.getGuiScale().getValue();
        double scaleProgress = (double) currentScale / maxScale;

        widgets.add(new SupersonicSlider(widgetX - 60, currentY, 120, 16, Text.empty(), scaleProgress, "", currentScale == 0 ? "Auto" : currentScale + "x") {
            @Override
            protected void applyValue() {
                int newScale = (int) Math.round(this.value * maxScale);
                client.options.getGuiScale().setValue(newScale);
                client.onResolutionChanged(); // Forces real-time UI scale update
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                int scale = client.options.getGuiScale().getValue();
                this.setMessage(Text.literal(scale == 0 ? "Auto" : scale + "x"));
            }
        });
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int contentX = x + 200;

        // Headers
        context.drawText(client.textRenderer, "HUD & OVERLAY", contentX, y + 20, 0xFFFFFF, true);
        context.drawText(client.textRenderer, "Customize your in-game HUD and overlays.", contentX, y + 35, 0xAAAAAA, false);

        context.drawText(client.textRenderer, "HUD ELEMENTS", contentX, y + 55, 0x00FFFF, false);

        int textY = y + 75;
        context.drawText(client.textRenderer, "Armor HUD", contentX, textY, 0xFFFFFF, false);
        textY += 32;
        context.drawText(client.textRenderer, "Potion HUD", contentX, textY, 0xFFFFFF, false);
        textY += 32;
        context.drawText(client.textRenderer, "FPS Counter", contentX, textY, 0xFFFFFF, false);
        textY += 32;
        context.drawText(client.textRenderer, "CPS Counter", contentX, textY, 0xFFFFFF, false);

        textY += 40;
        context.drawText(client.textRenderer, "HUD APPEARANCE", contentX, textY, 0x00FFFF, false);
        textY += 20;
        context.drawText(client.textRenderer, "HUD Scale", contentX, textY, 0xFFFFFF, false);
        
        // Draw HUD Preview Right Panel
        drawHudPreviewPanel(context, x + 720, y + 20);
    }

    private void drawHudPreviewPanel(DrawContext context, int panelX, int panelY) {
        context.fill(panelX, panelY, panelX + 220, panelY + 200, 0xFF0A111A);
        context.drawBorder(panelX, panelY, 220, 200, 0xFF1A3344);
        context.drawText(MinecraftClient.getInstance().textRenderer, "HUD PREVIEW", panelX + 10, panelY + 10, 0x00FFFF, false);
        
        // Mockup values for preview
        context.drawText(MinecraftClient.getInstance().textRenderer, "X: 123", panelX + 15, panelY + 30, 0xFFFFFF, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, "Y: 64", panelX + 15, panelY + 40, 0xFFFFFF, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, "Z: -456", panelX + 15, panelY + 50, 0xFFFFFF, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, "4000+ FPS", panelX + 150, panelY + 30, 0xFFFFFF, false);
    }
}
