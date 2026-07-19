package net.supersonic.gui.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class SupersonicToggle extends ClickableWidget {
    private boolean toggled;
    private final ToggleAction action;

    public SupersonicToggle(int x, int y, int width, int height, boolean initialState, ToggleAction action) {
        super(x, y, width, height, Text.empty());
        this.toggled = initialState;
        this.action = action;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        int color = this.toggled ? 0xFF00E5FF : 0xFF333333; // Cyan for ON, Dark Gray for OFF
        int knobColor = 0xFFFFFFFF;
        
        // Draw background pill (simplified as a rectangle for now, use textures for perfect curves)
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color);
        
        // Draw knob
        int knobX = this.toggled ? this.getX() + this.width - this.height + 2 : this.getX() + 2;
        context.fill(knobX, this.getY() + 2, knobX + this.height - 4, this.getY() + this.height - 4, knobColor);
        
        // Draw Text inside
        String text = this.toggled ? "ON" : "OFF";
        int textX = this.toggled ? this.getX() + 4 : this.getX() + 16;
        context.drawText(context.textRenderer, text, textX, this.getY() + (this.height - 8) / 2, 0xFFFFFF, false);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.toggled = !this.toggled;
        this.action.onToggle(this.toggled);
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {}

    public interface ToggleAction {
        void onToggle(boolean newState);
    }
}
