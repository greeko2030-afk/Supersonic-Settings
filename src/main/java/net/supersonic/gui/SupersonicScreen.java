package net.supersonic.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.supersonic.gui.tabs.*;
import net.minecraft.client.MinecraftClient;

public class SupersonicScreen extends Screen {
    private String activeTab = "Performance"; 
    private final int startX = 40;
    private final int startY = 30;
    
    // Tab Instances
    private PerformanceTab performanceTab;
    private AudioTab audioTab;
    private VisualsTab visualsTab;
    // Other tabs would be initialized here...

    public SupersonicScreen() {
        super(Text.literal("Supersonic Settings"));
    }

    @Override
    protected void init() {
        this.clearChildren();
        
        // Initialize active tab with real functional widgets
        switch (activeTab) {
            case "Performance":
                this.performanceTab = new PerformanceTab(startX, startY);
                this.performanceTab.init(this.children(), this.client);
                break;
            case "Audio":
                this.audioTab = new AudioTab(startX, startY);
                this.audioTab.init(this.children(), this.client);
                break;
            case "Visuals":
                this.visualsTab = new VisualsTab(startX, startY);
                this.visualsTab.init(this.children(), this.client);
                break;
            // Add cases for HUD, Multiplayer, Controls, etc.
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Main Background (Dark Blue/Black transparent theme)
        context.fill(startX, startY, startX + 950, startY + 540, 0xFA050A10);
        context.drawBorder(startX, startY, 950, 540, 0xFF00A2FF);

        // Render Sidebar
        renderSidebar(context, mouseX, mouseY);

        // Render Active Tab Content
        if (activeTab.equals("Performance") && performanceTab != null) performanceTab.render(context, mouseX, mouseY, delta);
        if (activeTab.equals("Audio") && audioTab != null) audioTab.render(context, mouseX, mouseY, delta);
        if (activeTab.equals("Visuals") && visualsTab != null) visualsTab.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSidebar(DrawContext context, int mouseX, int mouseY) {
        int sidebarWidth = 180;
        context.fill(startX, startY, startX + sidebarWidth, startY + 540, 0xFF0A111A);
        
        // Header Text
        context.drawText(this.textRenderer, "SUPERSONIC SETTINGS", startX + 45, startY + 20, 0x00FFFF, true);
        context.drawText(this.textRenderer, "Optimize. Accelerate. Dominate.", startX + 45, startY + 32, 0xAAAAAA, false);
        
        // Render Tab Buttons (Visual representation)
        String[] tabs = {"Gameplay", "Visuals", "Performance", "HUD & Overlay", "Audio", "Multiplayer", "Controls", "Mods & Addons", "Replay & Capture", "Supersonic AI"};
        int btnY = startY + 70;
        
        for (String tab : tabs) {
            boolean isActive = activeTab.equals(tab);
            int color = isActive ? 0xFF00A2FF : 0xFF223344;
            int textColor = isActive ? 0xFFFFFFFF : 0xFFAAAAAA;
            
            // Button Background
            context.fill(startX + 10, btnY, startX + sidebarWidth - 10, btnY + 30, color);
            context.drawText(this.textRenderer, tab, startX + 45, btnY + 11, textColor, false);
            btnY += 35;
        }
    }
}
