package net.supersonic.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;

public class SupersonicDropdown extends PressableWidget {
    private final OnClick onClick;

    public interface OnClick {
        void onClick(SupersonicDropdown button);
    }

    public SupersonicDropdown(int x, int y, int width, int height, Text message, OnClick onClick) {
        super(x, y, width, height, message);
        this.onClick = onClick;
    }

    @Override
    public void onPress() {
        if (this.onClick != null) {
            this.onClick.onClick(this);
        }
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Draw dropdown background box
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF222222);
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, 0xFF00FFFF);

        // Draw text safely using MinecraftClient textRenderer
        context.drawText(client.textRenderer, this.getMessage(), this.getX() + 10, this.getY() + (this.height - 8) / 2, 0xFFFFFF, false);
    }
}
