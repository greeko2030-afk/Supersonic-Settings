package net.supersonic.gui.tabs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class ReplayCaptureTab {
    private final int x;
    private final int y;

    public ReplayCaptureTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets) {
        int leftColX = x + 180;
        int rightColX = x + 530;
        int settingX = leftColX + 270;
        
        // --- REPLAY SETTINGS ---
        int currentY = y + 70;
        widgets.add(new SupersonicToggle(settingX, currentY, 40, 16, true, state -> {}));
        currentY += 28;
        widgets.add(new SupersonicDropdown(settingX - 50, currentY, 90, 16, Text.literal("5 Minutes"), btn -> {}));
        currentY += 28;
        widgets.add(new SupersonicDropdown(settingX - 50, currentY, 90, 16, Text.literal("2048 MB"), btn -> {}));
        currentY += 28;
        widgets.add(new SupersonicDropdown(settingX - 50, currentY, 90, 16, Text.literal("High"), btn -> {}));
        currentY += 28;
        widgets.add(new SupersonicDropdown(settingX - 50, currentY, 90, 16, Text.literal("Full"), btn -> {}));
        currentY += 28;
        widgets.add(new SupersonicToggle(settingX, currentY, 40, 16, true, state -> {}));
        currentY += 28;
        widgets.add(new SupersonicToggle(settingX, currentY, 40, 16, true, state -> {}));
        currentY += 28;
        widgets.add(new SupersonicDropdown(settingX - 50, currentY, 90, 16, Text.literal(".mcpr"), btn -> {}));

        // --- CAMERA CONTROLS ---
        currentY += 45;
        widgets.add(ButtonWidget.builder(Text.literal("C"), btn -> {}).dimensions(settingX - 10, currentY, 50, 16).build());
        currentY += 28;
        widgets.add(new SupersonicSlider(settingX - 120, currentY, 120, 16, Text.empty(), 0.5, "", "1.25x"));
        currentY += 28;
        widgets.add(new SupersonicSlider(settingX - 120, currentY, 120, 16, Text.empty(), 0.5, "", "1.00x"));
        currentY += 28;
        widgets.add(new SupersonicToggle(settingX, currentY, 40, 16, true, state -> {}));
        currentY += 28;
        widgets.add(new SupersonicDropdown(settingX - 50, currentY, 90, 16, Text.literal("16 Chunks"), btn -> {}));

        // --- RIGHT PANEL (Replay Preview & Shortcuts) ---
        // Playback buttons
        int prevY = y + 330;
        widgets.add(ButtonWidget.builder(Text.literal("◀◀"), btn -> {}).dimensions(rightColX + 10, prevY, 30, 20).build());
        widgets.add(ButtonWidget.builder(Text.literal("◀"), btn -> {}).dimensions(rightColX + 45, prevY, 30, 20).build());
        widgets.add(ButtonWidget.builder(Text.literal("▶"), btn -> {}).dimensions(rightColX + 80, prevY, 30, 20).build());
        widgets.add(ButtonWidget.builder(Text.literal("▶▶"), btn -> {}).dimensions(rightColX + 115, prevY, 30, 20).build());
        widgets.add(new SupersonicDropdown(rightColX + 155, prevY, 50, 20, Text.literal("1.0x"), btn -> {}));
        widgets.add(ButtonWidget.builder(Text.literal("📷"), btn -> {}).dimensions(rightColX + 210, prevY, 20, 20).build());

        // Shortcuts
        int shortY = y + 380;
        addShortcut(widgets, rightColX, shortY, "SPACE", "J", "=", "-");
        addShortcut(widgets, rightColX + 120, shortY, "B", "C", "V", "P");
    }

    private void addShortcut(List<ClickableWidget> widgets, int x, int y, String b1, String b2, String b3, String b4) {
        widgets.add(ButtonWidget.builder(Text.literal(b1), btn -> {}).dimensions(x + 100, y, 40, 16).build());
        widgets.add(ButtonWidget.builder(Text.literal(b2), btn -> {}).dimensions(x + 100, y + 22, 40, 16).build());
        widgets.add(ButtonWidget.builder(Text.literal(b3), btn -> {}).dimensions(x + 100, y + 44, 40, 16).build());
        widgets.add(ButtonWidget.builder(Text.literal(b4), btn -> {}).dimensions(x + 100, y + 66, 40, 16).build());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int leftColX = x + 180;
        int rightColX = x + 530;
        
        // --- HEADER ---
        context.drawText(context.textRenderer, "REPLAY & CAPTURE", leftColX + 25, y + 20, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Record, review and share your best moments.", leftColX + 25, y + 35, 0xAAAAAA, false);
        context.fill(leftColX, y + 20, leftColX + 16, y + 36, 0xFF00FFFF);

        // --- LEFT COLUMN ---
        int currentY = y + 55;
        // Sub-tabs
        context.drawText(context.textRenderer, "REPLAY", leftColX + 5, currentY, 0xFFFFFF, false);
        context.drawText(context.textRenderer, "RECORDING", leftColX + 65, currentY, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "SCREENSHOT", leftColX + 135, currentY, 0xAAAAAA, false);
        context.fill(leftColX, currentY + 12, leftColX + 45, currentY + 14, 0xFF00FFFF); // Active

        currentY += 30;
        context.drawText(context.textRenderer, "REPLAY SETTINGS", leftColX, currentY, 0x00FFFF, false);
        drawSetting(context, leftColX, currentY + 20, "Enable Replay Mod", "Enables the in-game replay system.");
        drawSetting(context, leftColX, currentY + 48, "Replay Buffer Duration", "How long to keep the replay buffer.");
        drawSetting(context, leftColX, currentY + 76, "Max Memory Usage", "Memory limit for replay buffer.");
        drawSetting(context, leftColX, currentY + 104, "Replay Quality", "Higher quality uses more storage.");
        drawSetting(context, leftColX, currentY + 132, "Entity Handling", "Choose how entities are handled in replays.");
        drawSetting(context, leftColX, currentY + 160, "Include Sounds", "Record in-game sounds in replays.");
        drawSetting(context, leftColX, currentY + 188, "Camera Path Smoothing", "Smooth camera movements in replay.");
        drawSetting(context, leftColX, currentY + 216, "Replay File Format", "Format for saved replay files.");

        currentY += 260;
        context.drawText(context.textRenderer, "CAMERA CONTROLS (REPLAY VIEW)", leftColX, currentY, 0x00FFFF, false);
        drawSetting(context, leftColX, currentY + 20, "Free Camera", "Enable free camera movement.");
        drawSetting(context, leftColX, currentY + 48, "Camera Speed", "Adjust camera movement speed.");
        drawSetting(context, leftColX, currentY + 76, "Rotate Speed", "Adjust camera rotation speed.");
        drawSetting(context, leftColX, currentY + 104, "Hide HUD in Replay", "Hide HUD elements while in replay.");
        drawSetting(context, leftColX, currentY + 132, "Render Distance in Replay", "Render distance while viewing replays.");

        // --- RIGHT PANEL ---
        DrawPanelBox(context, rightColX, y + 55, 240, 260, "REPLAY PREVIEW");
        context.fill(rightColX + 10, y + 75, rightColX + 230, y + 250, 0xFF000000); // Video Preview Placeholder
        context.drawText(context.textRenderer, "00:01:24 / 00:05:00", rightColX + 130, y + 60, 0xAAAAAA, false);

        DrawPanelBox(context, rightColX, y + 320, 240, 100, "REPLAY SHORTCUTS");
        // Labels for shortcuts
        context.drawText(context.textRenderer, "Play / Pause", rightColX + 10, y + 340, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Stop Replay", rightColX + 10, y + 362, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Increase Speed", rightColX + 10, y + 384, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Decrease Speed", rightColX + 10, y + 406, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Prev Keyframe", rightColX + 130, y + 340, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Toggle Camera", rightColX + 130, y + 362, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Save Replay", rightColX + 130, y + 384, 0xCCCCCC, false);
        context.drawText(context.textRenderer, "Export Replay", rightColX + 130, y + 406, 0xCCCCCC, false);
    }

    private void drawSetting(DrawContext context, int x, int y, String title, String desc) {
        context.drawText(context.textRenderer, title, x, y, 0xFFFFFF, false);
        context.drawText(context.textRenderer, desc, x, y + 10, 0x777777, false);
    }

    private void DrawPanelBox(DrawContext context, int x, int y, int width, int height, String title) {
        context.fill(x, y, x + width, y + height, 0xFF0A111A);
        context.drawBorder(x, y, width, height, 0xFF1A3344);
        context.drawText(context.textRenderer, title, x + 10, y + 10, 0x00FFFF, false);
    }
}
