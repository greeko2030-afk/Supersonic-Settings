package net.supersonic.gui.tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.text.Text;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class GameplayTab {
    private final int x, y;

    public GameplayTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets, MinecraftClient client) {
        int contentX = x + 200;
        int widgetX = contentX + 380;
        int currentY = y + 70;

        // --- GENERAL SECTION ---

        // Real Attack Indicator (Crosshair / Hotbar / Off)
        String currentIndicator = client.options.getAttackIndicator().getValue().toString();
        widgets.add(new SupersonicDropdown(widgetX - 40, currentY, 100, 16, Text.literal(currentIndicator), btn -> {
            // Logic to cycle through indicator modes could go here
            AttackIndicator next = client.options.getAttackIndicator().getValue() == AttackIndicator.CROSSHAIR 
                ? AttackIndicator.HOTBAR : AttackIndicator.CROSSHAIR;
            client.options.getAttackIndicator().setValue(next);
            btn.setMessage(Text.literal(next.toString()));
        }));
        currentY += 32;

        // Real Auto Jump Toggle
        boolean isAutoJump = client.options.getAutoJump().getValue();
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, isAutoJump, state -> {
            client.options.getAutoJump().setValue(state);
        }));
        currentY += 32;

        // Real Sprint Toggle
        boolean isSprintToggled = client.options.getSprintToggled().getValue();
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, isSprintToggled, state -> {
            client.options.getSprintToggled().setValue(state);
        }));
        currentY += 32;

        // Real Sneak Toggle
        boolean isSneakToggled = client.options.getSneakToggled().getValue();
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, isSneakToggled, state -> {
            client.options.getSneakToggled().setValue(state);
        }));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int contentX = x + 200;
        MinecraftClient client = MinecraftClient.getInstance();

        // Header
        context.drawText(client.textRenderer, "GAMEPLAY", contentX, y + 20, 0xFFFFFF, true);
        context.drawText(client.textRenderer, "Customize your in-game experience.", contentX, y + 35, 0xAAAAAA, false);

        int currentY = y + 75;

        // General Section Text
        context.drawText(client.textRenderer, "Attack Indicator", contentX, currentY, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Show attack cooldown indicator.", contentX, currentY + 10, 0x777777, false);
        currentY += 32;

        context.drawText(client.textRenderer, "Auto Jump", contentX, currentY, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Automatically jump when moving forward.", contentX, currentY + 10, 0x777777, false);
        currentY += 32;

        context.drawText(client.textRenderer, "Sprint Toggle", contentX, currentY, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Toggle sprint on key press.", contentX, currentY + 10, 0x777777, false);
        currentY += 32;

        context.drawText(client.textRenderer, "Sneak Toggle", contentX, currentY, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Toggle sneak on key press.", contentX, currentY + 10, 0x777777, false);

        // Draw Right Panel (Engine Status)
        drawEnginePanel(context, x + 720, y + 20);
    }

    private void drawEnginePanel(DrawContext context, int panelX, int panelY) {
        context.fill(panelX, panelY, panelX + 200, panelY + 480, 0xFF0A111A);
        context.drawBorder(panelX, panelY, 200, 480, 0xFF1A3344);
        
        context.drawText(MinecraftClient.getInstance().textRenderer, "SUPERSONIC ENGINE", panelX + 35, panelY + 15, 0xFFFFFF, true);
        
        // Cube Placeholder
        context.fill(panelX + 50, panelY + 50, panelX + 150, panelY + 150, 0xFF0055AA);
        
        // Engine Stats
        int statY = panelY + 180;
        context.drawText(MinecraftClient.getInstance().textRenderer, "RENDERER", panelX + 15, statY, 0xAAAAAA, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, "Supersonic Engine", panelX + 15, statY + 10, 0x00FFFF, false);
        
        statY += 40;
        context.drawText(MinecraftClient.getInstance().textRenderer, "FPS (AVERAGE)", panelX + 15, statY, 0xAAAAAA, false);
        context.getMatrices().push();
        context.getMatrices().scale(1.5f, 1.5f, 1.5f);
        context.drawText(MinecraftClient.getInstance().textRenderer, MinecraftClient.getInstance().getCurrentFps() + "+ FPS", (int)((panelX + 15) / 1.5f), (int)((statY + 10) / 1.5f), 0x00FFFF, true);
        context.getMatrices().pop();
    }
}
