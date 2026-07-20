package net.supersonic.gui.tabs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.supersonic.gui.widgets.SupersonicSlider;
import java.util.List;

public class AudioTab {
    private final int x, y;

    public AudioTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets, MinecraftClient client) {
        int contentX = x + 200;
        int widgetX = contentX + 380;
        int startY = y + 70;

        // Real Master Volume
        widgets.add(createSoundSlider(client, SoundCategory.MASTER, widgetX, startY));
        startY += 30;
        
        // Real Music Volume
        widgets.add(createSoundSlider(client, SoundCategory.MUSIC, widgetX, startY));
        startY += 30;
        
        // Real Record/Jukebox Volume
        widgets.add(createSoundSlider(client, SoundCategory.RECORDS, widgetX, startY));
        startY += 30;

        // Real Weather Volume
        widgets.add(createSoundSlider(client, SoundCategory.WEATHER, widgetX, startY));
    }

    private SupersonicSlider createSoundSlider(MinecraftClient client, SoundCategory category, int x, int y) {
        double currentVol = client.options.getSoundVolume(category);
        return new SupersonicSlider(x, y, 120, 16, Text.empty(), currentVol, "", Math.round(currentVol * 100) + "%") {
            @Override
            protected void applyValue() {
                client.options.getSoundVolumeOption(category).setValue(this.value);
                this.updateMessage();
            }
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(Math.round(this.value * 100) + "%"));
            }
        };
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int contentX = x + 200;
        context.drawText(MinecraftClient.getInstance().textRenderer, "AUDIO", contentX, y + 20, 0xFFFFFF, true);
        context.drawText(MinecraftClient.getInstance().textRenderer, "Fine tune your in-game sound experience.", contentX, y + 35, 0xAAAAAA, false);
        
        // Texts for the sliders
        int textY = y + 75;
        context.drawText(MinecraftClient.getInstance().textRenderer, "Master Volume", contentX, textY, 0xFFFFFF, false);
        textY += 30;
        context.drawText(MinecraftClient.getInstance().textRenderer, "Music", contentX, textY, 0xFFFFFF, false);
        textY += 30;
        context.drawText(MinecraftClient.getInstance().textRenderer, "Jukebox / Note Blocks", contentX, textY, 0xFFFFFF, false);
        textY += 30;
        context.drawText(MinecraftClient.getInstance().textRenderer, "Weather", contentX, textY, 0xFFFFFF, false);
    }
}
