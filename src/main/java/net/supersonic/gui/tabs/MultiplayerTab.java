package net.supersonic.gui.tabs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import net.supersonic.gui.widgets.SupersonicSlider;
import net.supersonic.gui.widgets.SupersonicDropdown;
import java.util.List;

public class MultiplayerTab {
    private final int x;
    private final int y;

    public MultiplayerTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void init(List<ClickableWidget> widgets) {
        int leftColX = x + 180; 
        int rightColX = x + 515;
        int settingSpacing = 28; 
        
        int controlX = leftColX + 270;

        // ================= LEFT COLUMN =================
        
        // --- CONNECTION & NETWORK ---
        int currentY = y + 70;
        widgets.add(new SupersonicToggle(controlX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(controlX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(controlX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(controlX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicSlider(controlX - 80, currentY, 120, 16, Text.empty(), 0.3, "", " 3 sec"));

        // --- PLAYER & CHAT ---
        currentY += 42;
        widgets.add(ButtonWidget.builder(Text.literal("Customize"), btn -> {}).dimensions(controlX - 35, currentY, 75, 16).build());
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(controlX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(controlX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(ButtonWidget.builder(Text.literal("Edit"), btn -> {}).dimensions(controlX - 10, currentY, 50, 16).build());
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(controlX, currentY, 40, 16, true, state -> {}));

        // --- MULTIPLAYER HUD ---
        currentY += 42;
        widgets.add(new SupersonicToggle(controlX, currentY, 40, 16, true, state -> {}));
        currentY += settingSpacing;
        widgets.add(ButtonWidget.builder(Text.literal("Customize"), btn -> {}).dimensions(controlX - 35, currentY, 75, 16).build());
        currentY += settingSpacing;
        widgets.add(new SupersonicToggle(controlX, currentY, 40, 16, true, state -> {}));


        // ================= RIGHT COLUMN =================
        
        int rightPanelWidth = 265;
        
        // --- QUICK CONNECT ---
        int quickConnectY = y + 250;
        widgets.add(ButtonWidget.builder(Text.literal("Add Server"), btn -> {}).dimensions(rightColX + 10, quickConnectY, 115, 20).build());
        widgets.add(ButtonWidget.builder(Text.literal("Edit Servers"), btn -> {}).dimensions(rightColX + 135, quickConnectY, 115, 20).build());
        widgets.add(ButtonWidget.builder(Text.literal("Direct Connect"), btn -> {}).dimensions(rightColX + 10, quickConnectY + 25, 115, 20).build());
        widgets.add(ButtonWidget.builder(Text.literal("Delete Servers"), btn -> {}).dimensions(rightColX + 135, quickConnectY + 25, 115, 20).build());

        // --- MULTIPLAYER SETTINGS PRESETS ---
        int presetsY = y + 345;
        widgets.add(new SupersonicDropdown(rightColX + 10, presetsY, 115, 20, Text.literal("Default"), btn -> {}));
        widgets.add(ButtonWidget.builder(Text.literal("PvP Optimized"), btn -> {}).dimensions(rightColX + 135, presetsY, 115, 20).build());
        widgets.add(ButtonWidget.builder(Text.literal("Vanilla+"), btn -> {}).dimensions(rightColX + 10, presetsY + 25, 115, 20).build());
        widgets.add(ButtonWidget.builder(Text.literal("Minigames"), btn -> {}).dimensions(rightColX + 135, presetsY + 25, 115, 20).build());
        widgets.add(ButtonWidget.builder(Text.literal("Custom Profile"), btn -> {}).dimensions(rightColX + 10, presetsY + 50, 240, 20).build());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int leftColX = x + 180;
        int rightColX = x + 515;
        int rightPanelWidth = 265;
        int settingSpacing = 28;
        
        // --- HEADER ---
        context.drawText(context.textRenderer, "MULTIPLAYER", leftColX + 25, y + 20, 0xFFFFFF, true);
        context.drawText(context.textRenderer, "Enhance your multiplayer experience.", leftColX + 25, y + 35, 0xAAAAAA, false);
        
        // Icon Placeholder
        context.fill(leftColX, y + 20, leftColX + 16, y + 36, 0xFF00FFFF);

        // ================= LEFT COLUMN TEXT RENDERING =================
        
        int currentY = y + 55;
        context.drawText(context.textRenderer, "CONNECTION & NETWORK", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSetting(context, leftColX, currentY, "Smart Ping Optimization", "Reduces latency and optimizes your connection.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Reduced Packet Delay", "Optimizes packet handling for faster response.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Server Lag Indicator", "Show real-time server TPS and lag status.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Auto Reconnect", "Automatically reconnect if you get disconnected.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Reconnect Delay", "Delay before attempting to reconnect.");

        currentY += 30;
        context.drawText(context.textRenderer, "PLAYER & CHAT", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSetting(context, leftColX, currentY, "Player List Customization", "Customize what to show in the player list.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Show Player Ping", "Display ping next to player names.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Chat Timestamps", "Show time next to chat messages.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Custom Chat Colors", "Customize chat colors and formats.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Mention Alerts", "Get notified when you are mentioned in chat.");

        currentY += 30;
        context.drawText(context.textRenderer, "MULTIPLAYER HUD", leftColX, currentY, 0x00FFFF, false);
        
        currentY += 15;
        drawSetting(context, leftColX, currentY, "Server Info HUD", "Show server IP, TPS, players and more.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Boss Bar Customization", "Customize boss bars in multiplayer.");
        currentY += settingSpacing;
        drawSetting(context, leftColX, currentY, "Tab List Enhancements", "Enhanced tab list with more info.");


        // ================= RIGHT COLUMN PANELS =================

        // --- SERVER INFO PREVIEW ---
        DrawPanelBox(context, rightColX, y + 55, rightPanelWidth, 160, "SERVER INFO PREVIEW");
        int previewY = y + 75;
        
        // Mockup Server Details 
        context.drawText(context.textRenderer, "NarratorMC", rightColX + 10, previewY, 0xFFFFFF, true);
        drawPingBar(context, rightColX + 215, previewY, 4, 0x00FF00); // 4 bars green
        context.drawText(context.textRenderer, "42ms", rightColX + 230, previewY, 0x00FF00, false);
        
        context.drawText(context.textRenderer, "Online: 132/500", rightColX + 10, previewY + 12, 0xAAAAAA, false);
        context.drawText(context.textRenderer, "TPS: 19.96", rightColX + 205, previewY + 12, 0xAAAAAA, false);
        
        // Player List Mockup
        int listY = previewY + 30;
        drawPlayerEntry(context, rightColX, listY, "OWNER", 0xFF5555, "Rafiee_playssMC", "38ms", 4);
        listY += 16;
        drawPlayerEntry(context, rightColX, listY, "ADMIN", 0x5555FF, "ChatGPT", "42ms", 4);
        listY += 16;
        drawPlayerEntry(context, rightColX, listY, "MOD", 0x55FF55, "Hanjala", "45ms", 4);
        listY += 16;
        drawPlayerEntry(context, rightColX, listY, "YT", 0xFF5555, "NotRexy_", "39ms", 4);
        listY += 16;
        drawPlayerEntry(context, rightColX, listY, "MVP+", 0xFFAA00, "ZyluxXD", "41ms", 4);
        
        context.drawText(context.textRenderer, "... and 127 more players", rightColX + 40, listY + 16, 0x777777, false);

        // --- QUICK CONNECT ---
        DrawPanelBox(context, rightColX, y + 225, rightPanelWidth, 80, "QUICK CONNECT");

        // --- MULTIPLAYER SETTINGS PRESETS ---
        DrawPanelBox(context, rightColX, y + 315, rightPanelWidth, 115, "MULTIPLAYER SETTINGS PRESETS");
    }

    private void drawSetting(DrawContext context, int x, int y, String title, String desc) {
        int textX = x + 20; 
        context.drawText(context.textRenderer, title, textX, y - 1, 0xFFFFFF, false);
        context.drawText(context.textRenderer, desc, textX, y + 9, 0x777777, false);
        
        // Icon placeholder
        context.fill(x, y + 2, x + 12, y + 14, 0xFF224466);
    }

    private void drawPlayerEntry(DrawContext context, int x, int y, String rank, int rankColor, String name, String pingTxt, int bars) {
        // Player Head Mockup (square)
        context.fill(x + 10, y, x + 18, y + 8, 0xFFAA5533); 
        
        context.drawText(context.textRenderer, rank, x + 25, y, rankColor, false);
        context.drawText(context.textRenderer, name, x + 65, y, 0xFFFFFF, false);
        
        drawPingBar(context, x + 215, y, bars, 0x00FF00);
        context.drawText(context.textRenderer, pingTxt, x + 230, y, 0x00FF00, false);
    }

    private void drawPingBar(DrawContext context, int x, int y, int amount, int color) {
        // Mockup for signal bars (like WiFi)
        for (int i = 0; i < 4; i++) {
            int barHeight = 2 + (i * 2);
            int barColor = (i < amount) ? color : 0xFF555555;
            context.fill(x + (i * 3), y + 8 - barHeight, x + (i * 3) + 2, y + 8, barColor);
        }
    }

    private void DrawPanelBox(DrawContext context, int x, int y, int width, int height, String title) {
        context.fill(x, y, x + width, y + height, 0xFF0A111A);
        context.drawBorder(x, y, width, height, 0xFF1A3344);
        context.drawText(context.textRenderer, title, x + 10, y + 10, 0x00FFFF, false);
    }
}
