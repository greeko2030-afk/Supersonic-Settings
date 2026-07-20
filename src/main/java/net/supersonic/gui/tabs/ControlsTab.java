package net.supersonic.gui.tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.supersonic.gui.widgets.SupersonicToggle;
import java.util.List;

public class ControlsTab {
    private final int x, y;
    private KeyBinding selectedKeyBinding = null;

    public ControlsTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets, MinecraftClient client) {
        int contentX = x + 200;
        int currentY = y + 70;

        // --- REAL MOVEMENT KEYBINDS ---
        KeyBinding forwardKey = client.options.forwardKey;
        KeyBinding backKey = client.options.backKey;
        KeyBinding leftKey = client.options.leftKey;
        KeyBinding rightKey = client.options.rightKey;

        widgets.add(createKeybindButton(forwardKey, contentX + 280, currentY));
        currentY += 25;
        widgets.add(createKeybindButton(backKey, contentX + 280, currentY));
        currentY += 25;
        widgets.add(createKeybindButton(leftKey, contentX + 280, currentY));
        currentY += 25;
        widgets.add(createKeybindButton(rightKey, contentX + 280, currentY));

        // --- KEYBIND OPTIONS (RIGHT PANEL) ---
        int rightPanelX = x + 720;
        int optY = y + 250;

        // Real Toggle Sprint/Sneak (Vanilla features)
        boolean sprintToggled = client.options.getSprintToggled().getValue();
        widgets.add(new SupersonicToggle(rightPanelX + 130, optY, 40, 16, sprintToggled, state -> {
            client.options.getSprintToggled().setValue(state);
        }));
        
        optY += 40;
        boolean sneakToggled = client.options.getSneakToggled().getValue();
        widgets.add(new SupersonicToggle(rightPanelX + 130, optY, 40, 16, sneakToggled, state -> {
            client.options.getSneakToggled().setValue(state);
        }));
    }

    private ButtonWidget createKeybindButton(KeyBinding keyBinding, int btnX, int btnY) {
        return ButtonWidget.builder(keyBinding.getBoundKeyLocalizedText(), button -> {
            this.selectedKeyBinding = keyBinding;
            button.setMessage(Text.literal("> " + keyBinding.getBoundKeyLocalizedText().getString() + " <"));
        }).dimensions(btnX, btnY, 80, 20).build();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int contentX = x + 200;

        context.drawText(client.textRenderer, "CONTROLS", contentX, y + 20, 0xFFFFFF, true);
        context.drawText(client.textRenderer, "MOVEMENT", contentX, y + 50, 0x00FFFF, false);

        int textY = y + 75;
        context.drawText(client.textRenderer, "Move Forward", contentX, textY, 0xFFFFFF, false);
        textY += 25;
        context.drawText(client.textRenderer, "Move Backward", contentX, textY, 0xFFFFFF, false);
        textY += 25;
        context.drawText(client.textRenderer, "Move Left", contentX, textY, 0xFFFFFF, false);
        textY += 25;
        context.drawText(client.textRenderer, "Move Right", contentX, textY, 0xFFFFFF, false);
        
        // Right Panel
        int rightPanelX = x + 720;
        context.drawText(client.textRenderer, "KEYBIND OPTIONS", rightPanelX, y + 230, 0x00FFFF, false);
        context.drawText(client.textRenderer, "Toggle Sprint", rightPanelX, y + 250, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Hold to Sneak", rightPanelX, y + 290, 0xFFFFFF, false);
    }
}
