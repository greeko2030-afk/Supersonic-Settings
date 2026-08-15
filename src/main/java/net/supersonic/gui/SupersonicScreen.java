package net.supersonic.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.supersonic.gui.tabs.*;

public class SupersonicScreen extends Screen {
    private int selectedTab = 0;
    private final GameplayTab gameplayTab;
    private final AudioTab audioTab;
    private final VisualsTab visualsTab;
    private final MultiplayerTab multiplayerTab;
    private final ControlsTab controlsTab;

    public SupersonicScreen() {
        super(Text.literal("Supersonic Settings"));
        int startX = 50;
        int startY = 30;
        this.gameplayTab = new GameplayTab(startX, startY);
        this.audioTab = new AudioTab(startX, startY);
        this.visualsTab = new VisualsTab(startX, startY);
        this.multiplayerTab = new MultiplayerTab(startX, startY);
        this.controlsTab = new ControlsTab(startX, startY);
    }

    @Override
    protected void init() {
        this.clearChildren();
        
        // Pass generic child elements list to avoid type casting mismatches
        if (selectedTab == 0) {
            this.gameplayTab.init(this.children(), this.client);
        } else if (selectedTab == 1) {
            this.audioTab.init(this.children(), this.client);
        } else if (selectedTab == 2) {
            this.visualsTab.init(this.children(), this.client);
        } else if (selectedTab == 3) {
            this.multiplayerTab.init(this.children(), this.client);
        } else if (selectedTab == 4) {
            this.controlsTab.init(this.children(), this.client);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        if (selectedTab == 0) {
            this.gameplayTab.render(context, mouseX, mouseY, delta);
        } else if (selectedTab == 1) {
            this.audioTab.render(context, mouseX, mouseY, delta);
        } else if (selectedTab == 2) {
            this.visualsTab.render(context, mouseX, mouseY, delta);
        } else if (selectedTab == 3) {
            this.multiplayerTab.render(context, mouseX, mouseY, delta);
        } else if (selectedTab == 4) {
            this.controlsTab.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
