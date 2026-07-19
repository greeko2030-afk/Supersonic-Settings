package net.supersonic.gui.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class SupersonicSlider extends SliderWidget {
    private final String prefix;
    private final String suffix;

    public SupersonicSlider(int x, int y, int width, int height, Text text, double value, String prefix, String suffix) {
        super(x, y, width, height, text, value);
        this.prefix = prefix;
        this.suffix = suffix;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        long displayValue = Math.round(this.value * 100);
        this.setMessage(Text.literal(prefix + displayValue + suffix));
    }

    @Override
    protected void applyValue() {
        // Here you would save the value to your config
    }
    
    @Override
    protected void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Custom styling to match your cyan neon theme
        context.fill(this.getX(), this.getY() + this.height / 2 - 1, this.getX() + this.width, this.getY() + this.height / 2 + 1, 0xFF335555);
        int fillWidth = (int) (this.value * (this.width - 8));
        context.fill(this.getX(), this.getY() + this.height / 2 - 1, this.getX() + fillWidth, this.getY() + this.height / 2 + 1, 0xFF00FFFF);
    }
}
