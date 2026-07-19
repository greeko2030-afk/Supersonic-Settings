package net.supersonic.gui.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SupersonicDropdown extends ButtonWidget {

    public SupersonicDropdown(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw Dark Background and Border
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF0A111A);
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, 0xFF334455);
        
        // Draw Selected Text
        context.drawText(context.textRenderer, this.getMessage(), this.getX() + 10, this.getY() + (this.height - 8) / 2, 0xFFFFFF, false);
        
        // Draw Dropdown Arrow 'v'
        context.drawText(context.textRenderer, "v", this.getX() + this.width - 15, this.getY() + (this.height - 8) / 2, 0xAAAAAA, false);
    }
}
