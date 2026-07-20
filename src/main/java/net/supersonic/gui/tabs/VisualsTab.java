package net.supersonic.gui.tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.AoMode;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.text.Text;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class VisualsTab {
    private final int x, y;

    public VisualsTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets, MinecraftClient client) {
        int contentX = x + 200;
        int widgetX = contentX + 380;
        int currentY = y + 70;

        // --- GRAPHICS QUALITY SECTION ---

        // Real Graphics Mode (Fast / Fancy / Fabulous)
        String currentGraphics = client.options.getGraphicsMode().getValue().toString();
        widgets.add(new SupersonicDropdown(widgetX - 40, currentY, 100, 16, Text.literal(currentGraphics), btn -> {
            GraphicsMode nextMode = client.options.getGraphicsMode().getValue() == GraphicsMode.FANCY ? GraphicsMode.FAST : GraphicsMode.FANCY;
            client.options.getGraphicsMode().setValue(nextMode);
            btn.setMessage(Text.literal(nextMode.toString()));
        }));
        currentY += 32;

        // Real Smooth Lighting Toggle (Off / Minimum / Maximum)
        boolean isSmoothLighting = client.options.getAo().getValue() == AoMode.MAX;
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, isSmoothLighting, state -> {
            client.options.getAo().setValue(state ? AoMode.MAX : AoMode.OFF);
        }));
        currentY += 32;

        // Real Entity Shadows Toggle
        boolean hasEntityShadows = client.options.getEntityShadows().getValue();
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, hasEntityShadows, state -> {
            client.options.getEntityShadows().setValue(state);
        }));
        currentY += 32;
        
        // Placeholder for Block Shadows (Requires custom shader hooks)
        widgets.add(new SupersonicToggle(widgetX, currentY, 40, 16, true, state -> {}));
        currentY += 32;

        // Real Cloud Quality (Off / Fast / Fancy)
        String cloudMode = client.options.getCloudRenderMode().getValue().toString();
        widgets.add(new SupersonicDropdown(widgetX - 40, currentY, 100, 16, Text.literal(cloudMode), btn -> {
            CloudRenderMode nextCloud = client.options.getCloudRenderMode().getValue() == CloudRenderMode.FANCY ? CloudRenderMode.FAST : CloudRenderMode.FANCY;
            client.options.getCloudRenderMode().setValue(nextCloud);
            btn.setMessage(Text.literal(nextCloud.toString()));
        }));

        currentY += 90; // Jump to ADVANCED section

        // --- ADVANCED SECTION ---

        // Real Render Distance Slider
        int currentRenderDist = client.options.getViewDistance().getValue();
        double renderProgress = (currentRenderDist - 2.0) / 30.0;
        widgets.add(new SupersonicSlider(widgetX - 60, currentY, 120, 16, Text.empty(), renderProgress, "", currentRenderDist + " Chunks") {
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
        currentY += 32;

        // Real Brightness Slider
        double currentBrightness = client.options.getGamma().getValue();
        widgets.add(new SupersonicSlider(widgetX - 60, currentY, 120, 16, Text.empty(), currentBrightness, "", Math.round(currentBrightness * 100) + "%") {
            @Override
            protected void applyValue() {
                client.options.getGamma().setValue(this.value);
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(Math.round(this.value * 100) + "%"));
            }
        });
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int contentX = x + 200;
        MinecraftClient client = MinecraftClient.getInstance();

        // Header
        context.drawText(client.textRenderer, "VISUALS", contentX, y + 20, 0xFFFFFF, true);

        int currentY = y + 75;

        context.drawText(client.textRenderer, "Graphics Quality", contentX, currentY, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Overall graphics quality level.", contentX, currentY + 10, 0x777777, false);
        currentY += 32;

        context.drawText(client.textRenderer, "Smooth Lighting", contentX, currentY, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Enable smooth lighting for better visuals.", contentX, currentY + 10, 0x777777, false);
        currentY += 32;

        context.drawText(client.textRenderer, "Entity Shadows", contentX, currentY, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Render shadows for entities.", contentX, currentY + 10, 0x777777, false);
        currentY += 32;

        context.drawText(client.textRenderer, "Block Shadows", contentX, currentY, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Render shadows for blocks.", contentX, currentY + 10, 0x777777, false);
        currentY += 32;

        context.drawText(client.textRenderer, "Cloud Quality", contentX, currentY, 0xFFFFFF, false);
        context.drawText(client.textRenderer, "Adjust cloud rendering quality.", contentX, currentY + 10, 0x777777, false);

        currentY += 55;
        context.drawText(client.textRenderer, "ADVANCED", contentX, currentY, 0x00FFFF, false);
        currentY += 20;

        context.drawText(client.textRenderer, "Render Distance", contentX, currentY, 0xFFFFFF, false);
        currentY += 32;

        context.drawText(client.textRenderer, "Brightness", contentX, currentY, 0xFFFFFF, false);
        
        // Right side Engine Panel is assumed to be handled similarly to Gameplay Tab.
    }
}
