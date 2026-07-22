package net.supersonic.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;

public class SupersonicToggle extends PressableWidget {
    private boolean state;
    private final OnStateChanged onStateChanged;

    public interface OnStateChanged {
        void onChange(boolean newState);
    }

    public SupersonicToggle(int x, int y, int width, int height, boolean initialState, OnStateChanged onStateChanged) {
        super(x, y, width, height, Text.empty());
        this.state = initialState;
        this.onStateChanged = onStateChanged;
        this.updateMessage();
    }

    @Override
    public void onPress() {
        this.state = !this.state;
        this.updateMessage();
        if (this.onStateChanged != null) {
            this.onStateChanged.onChange(this.state);
        }
    }

    private void updateMessage() {
        this.setMessage(Text.literal(this.state ? "ON" : "OFF"));
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int bgColor = this.state ? 0xFF00AA00 : 0xFFAA0000;
        
        // Draw toggle background box
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, 0xFFFFFFFF);

        // Draw text safely using MinecraftClient textRenderer
        int textColor = 0xFFFFFF;
        context.drawText(client.textRenderer, this.getMessage(), this.getX() + (this.width - client.textRenderer.getWidth(this.getMessage())) / 2, this.getY() + (this.height - 8) / 2, textColor, false);
    }
}
