package net.supersonic.gui.tabs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class AudioTab {
    private final int x;
    private final int y;

    public AudioTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets) {
        int leftColX = x + 180; 
        int rightColX = x + 530;
        int settingSpacing = 22; // Very tight spacing for the mixer
        
        int sliderX = leftColX + 200;
        int toggleX = leftColX + 270;

        // ================= LEFT COLUMN =================
        
        // --- VOLUME MIXER ---
        int currentY = y + 70;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.8, "", " 80%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.6, "", " 60%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.7, "", " 70%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.6, "", " 60%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.8, "", " 80%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.9, "", " 90%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.8, "", " 80%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.7, "", " 70%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.6, "", " 60%"));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(sliderX, currentY, 120, 14, Text.empty(), 0.85, "", " 85%"));

        // --- AUDIO ENHANCEMENTS ---
        currentY += 40;
        widgets.add(new SupersonicToggle(toggleX, currentY, 40, 14, true, state -> {}));
        currentY += settingSpacing + 2;
        widgets.add(new SupersonicToggle(toggleX, currentY, 40, 14, true, state -> {}));
        currentY += settingSpacing + 2;
        widgets.add(new SupersonicToggle(toggleX, currentY, 40, 14, true, state -> {}));
        currentY += settingSpacing + 2;
        widgets.add(new SupersonicToggle(toggleX, currentY, 40, 14, true, state -> {}));
        currentY += settingSpacing + 2;
        widgets.add(new SupersonicToggle(toggleX, currentY, 40, 14, true, state -> {}));
        currentY += settingSpacing + 2;
        widgets.add(new SupersonicDropdown(toggleX - 50, currentY - 2, 90, 16, Text.literal("Studio"), btn -> {}));


        // ================= RIGHT COLUMN =================
        
        int rightPanelWidth = 240;
        
        // --- AUDIO PROFILE ---
        int profileY = y + 55;
        widgets.add(new SupersonicDropdown(rightColX + 30, profileY + 55, 180, 16, Text.literal("Supersonic Immersive"), btn -> {}));
        widgets.add(ButtonWidget.builder(Text.literal("Save Profile"), btn -> {}).dimensions(rightColX + 10, profileY + 100, 105, 18).build());
        widgets.add(ButtonWidget.builder(Text.literal("Reset"), btn -> {}).dimensions(rightColX + 125, profileY + 100, 105, 18).build());

        // --- OUTPUT DEVICE ---
        int deviceY = y + 195;
        widgets.add(new SupersonicDropdown(rightColX + 10, deviceY + 25, 220, 16, Text.literal("Headphones (Realtek Audio)"), btn -> {}));
        widgets.add(new SupersonicSlider(rightColX + 110, deviceY + 50, 120, 14, Text.empty(), 0.8, "", " 80%"));
        widgets.add(ButtonWidget.builder(Text.literal("Test Audio"), btn -> {}).dimensions(rightColX + 10, deviceY + 70, 220, 18).build());

        // --- SOUND PREVIEW (Buttons only, icons rendered later) ---
        int previewY = y + 305;
        // The play buttons would be small ClickableWidgets, here we use generic buttons as placeholders
        for(int i = 0; i < 5; i++) {
            widgets.add(ButtonWidget.builder(Text.literal("▶"), btn -> {}).dimensions(rightColX + 140, previewY + 20 + (i * 18), 16, 16).build());
        }

        // --- MORE OPTIONS ---
        int optionsY = y + 420;
        widgets.add(new SupersonicToggle(rightColX + 190, optionsY + 20, 40, 14, false, state -> {}));
        widgets.add(new SupersonicToggle(rightColX + 190, optionsY + 40, 40, 14, true, state -> {}));
        widgets.add(new SupersonicToggle(rightColX + 190, optionsY + 60, 40, 14, true, state -> {}));
        widgets.add(new SupersonicDropdown(rightColX + 150, optionsY + 80, 80, 14, Text.literal("English (US)"), btn -> {}));
        widgets.add(new SupersonicToggle(rightColX + 190, optionsY + 100, 40, 14, true, state -> {}));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int leftColX = x + 180;
        int rightColX = x + 530;
        int rightPanelWidth = 240;
        int settingSpacing = 22;
        
        // --- HEADER ---
        context.drawText(context.textRenderer, "AUDIO", leftColX + 25, y + 20, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Fine tune your in-game sound experience.", leftColX + 25, y + 35, 0xAAAAAA, false);
        
        // Speaker Icon Placeholder
        context.fill(leftColX, y + 20, leftColX + 16, y + 36, 0xFF00FFFF);

        // ================= LEFT COLUMN TEXT RENDERING =================
        
        int currentY = y + 55;
        context.drawText(context.textRenderer, "VOLUME MIXER", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSettingCompact(context, leftColX, currentY, "Master Volume", "Overall game volume.");
        currentY += settingSpacing;
        drawSettingCompact(context, leftColX, currentY, "Music", "Adjust background music volume.");
        currentY += settingSpacing;
        drawSettingCompact(context, leftColX, currentY, "Jukebox / Note Blocks", "Music from jukebox and note blocks.");
        currentY += settingSpacing;
        drawSettingCompact(context, leftColX, currentY, "Weather", "Rain, thunder and weather sounds.");
        currentY += settingSpacing;
        drawSettingCompact(context, leftColX, currentY, "Blocks", "Block sounds and environment.");
        currentY += settingSpacing;
        drawSettingCompact(context, leftColX, currentY, "Hostile Creatures", "Hostile mobs and monster sounds.");
        currentY += settingSpacing;
        drawSettingCompact(context, leftColX, currentY, "Friendly Creatures", "Passive mobs and ambient creatures.");
        currentY += settingSpacing;
        drawSettingCompact(context, leftColX, currentY, "Players", "Other players' sounds.");
        currentY += settingSpacing;
        drawSettingCompact(context, leftColX, currentY, "Ambient / Environment", "Ambient sounds and atmosphere.");
        currentY += settingSpacing;
        drawSettingCompact(context, leftColX, currentY, "Voice Chat", "In-game voice chat volume.");

        currentY += 28;
        context.drawText(context.textRenderer, "AUDIO ENHANCEMENTS", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSettingCompact(context, leftColX, currentY, "Supersonic 3D Audio", "Immersive 3D positional audio.");
        currentY += settingSpacing + 2;
        drawSettingCompact(context, leftColX, currentY, "Bass Boost", "Enhance low frequency sounds.");
        currentY += settingSpacing + 2;
        drawSettingCompact(context, leftColX, currentY, "Treble Enhance", "Increase clarity of high frequencies.");
        currentY += settingSpacing + 2;
        drawSettingCompact(context, leftColX, currentY, "Dynamic Range Compression", "Balance loud and quiet sounds.");
        currentY += settingSpacing + 2;
        drawSettingCompact(context, leftColX, currentY, "Audio Normalization", "Normalize all sounds for consistency.");
        currentY += settingSpacing + 2;
        drawSettingCompact(context, leftColX, currentY, "Loudness Level", "Adjust overall loudness level.");


        // ================= RIGHT COLUMN PANELS =================

        // Helper to draw panel boxes
        DrawPanelBox(context, rightColX, y + 55, rightPanelWidth, 125, "AUDIO PROFILE");
        // Headphones Graphic Mockup
        context.fill(rightColX + 90, y + 75, rightColX + 150, y + 105, 0xFF005577);
        context.drawText(context.textRenderer, "Optimized for immersive gameplay", rightColX + 35, y + 130, 0x888888, false);

        DrawPanelBox(context, rightColX, y + 195, rightPanelWidth, 95, "OUTPUT DEVICE");
        context.drawText(context.textRenderer, "Output Volume", rightColX + 10, y + 252, 0xFFFFFF, false);

        DrawPanelBox(context, rightColX, y + 305, rightPanelWidth, 105, "SOUND PREVIEW");
        int previewItemY = y + 325;
        String[] previews = {"Rain & Thunder", "Cave Atmosphere", "Nether Ambient", "Battle / Combat", "Music Disc (Pigstep)"};
        for (int i = 0; i < previews.length; i++) {
            context.drawText(context.textRenderer, previews[i], rightColX + 25, previewItemY + (i * 18), 0xCCCCCC, false);
            // Equalizer mockup lines
            context.fill(rightColX + 165, previewItemY + (i * 18) + 2, rightColX + 210, previewItemY + (i * 18) + 6, 0xFF00FFFF);
        }

        DrawPanelBox(context, rightColX, y + 420, rightPanelWidth, 125, "MORE OPTIONS");
        int optY = y + 440;
        context.drawText(context.textRenderer, "Disable Music in Menus", rightColX + 10, optY, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Mute When Unfocused", rightColX + 10, optY + 20, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Streamer Mode (Remove Licenses)", rightColX + 10, optY + 40, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Audio Language", rightColX + 10, optY + 60, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Subtitles (Sounds)", rightColX + 10, optY + 80, 0xCCCCCC, false);
    }

    private void drawSettingCompact(DrawContext context, int x, int y, String title, String desc) {
        int textX = x + 20; 
        context.drawText(context.textRenderer, title, textX, y - 2, 0xFFFFFF, false);
        context.drawText(context.textRenderer, desc, textX, y + 7, 0x777777, false);
        
        // Icon placeholder
        context.fill(x, y, x + 12, y + 12, 0xFF224466);
    }

    private void DrawPanelBox(DrawContext context, int x, int y, int width, int height, String title) {
        context.fill(x, y, x + width, y + height, 0xFF0A111A);
        context.drawBorder(x, y, width, height, 0xFF1A3344);
        context.drawText(context.textRenderer, title, x + 10, y + 10, 0x00FFFF, false);
    }
}
