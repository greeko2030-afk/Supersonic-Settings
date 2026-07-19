package net.supersonic.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.supersonic.gui.tabs.PerformanceTab;

public class SupersonicScreen extends Screen {

    private final int UI_WIDTH = 900;
    private final int UI_HEIGHT = 600;
    private String activeTab = "Performance";
    
    private PerformanceTab performanceTab;
    private TextFieldWidget searchBar;

    public SupersonicScreen() {
        super(Text.literal("Supersonic Settings"));
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();

        int startX = (this.width - UI_WIDTH) / 2;
        int startY = (this.height - UI_HEIGHT) / 2;
        
        // Search Bar (Top Right)
        this.searchBar = new TextFieldWidget(this.textRenderer, startX + 500, startY + 15, 200, 20, Text.literal("Search settings..."));
        this.addDrawableChild(this.searchBar);

        // Sidebar Buttons
        String[] tabs = {"Gameplay", "Visuals", "Performance", "HUD & Overlay", "Audio", "Multiplayer", "Controls", "Mods & Addons", "Replay & Capture", "Supersonic AI"};
        int btnY = startY + 70;
        for (String tab : tabs) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal(tab), button -> {
                this.activeTab = tab;
                this.init(); // Reload UI
            }).dimensions(startX + 15, btnY, 140, 25).build());
            btnY += 30;
        }

        // Bottom Right Buttons (Apply, Reset, Done)
        int bottomBtnY = startY + UI_HEIGHT - 40;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), btn -> {}).dimensions(startX + 600, bottomBtnY, 80, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), btn -> {}).dimensions(startX + 690, bottomBtnY, 80, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> this.close()).dimensions(startX + 780, bottomBtnY, 80, 20).build());

        // Initialize Tab Content
        if (activeTab.equals("Performance")) {
            this.performanceTab = new PerformanceTab(startX, startY);
            // We pass the screen's addDrawableChild method reference to the tab so it can add its widgets
            this.performanceTab.init(this.children());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context); // Vanilla dim background

        int startX = (this.width - UI_WIDTH) / 2;
        int startY = (this.height - UI_HEIGHT) / 2;

        // 1. Draw Main UI Background (Dark Blue-ish gradient frame)
        context.fill(startX, startY, startX + UI_WIDTH, startY + UI_HEIGHT, 0xEE0A111A);
        context.drawBorder(startX, startY, UI_WIDTH, UI_HEIGHT, 0xFF00FFFF); // Cyan border
        
        // 2. Draw Sidebar Background
        context.fill(startX, startY, startX + 170, startY + UI_HEIGHT, 0xAA070B11);

        // 3. Draw Top Left Branding
        context.drawTextWithShadow(this.textRenderer, "SUPERSONIC SETTINGS", startX + 20, startY + 20, 0x00FFFF);
        context.drawText(this.textRenderer, "Optimize. Accelerate. Dominate.", startX + 20, startY + 32, 0xAAAAAA, false);

        // 4. Draw Bottom Left Engine Info (Supersonic Engine Active)
        int engineY = startY + UI_HEIGHT - 100;
        context.fill(startX + 10, engineY, startX + 160, engineY + 90, 0x44005555);
        context.drawText(this.textRenderer, "Supersonic Engine", startX + 20, engineY + 10, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Active", startX + 115, engineY + 10, 0x00FF00, false);
        context.drawText(this.textRenderer, "D3D12 Translation: ON", startX + 20, engineY + 30, 0xAAAAAA, false);
        context.drawText(this.textRenderer, "AIFI Frame Interpolation: ON", startX + 20, engineY + 45, 0xAAAAAA, false);
        context.drawText(this.textRenderer, "Nvidium+: ENABLED", startX + 20, engineY + 60, 0xAAAAAA, false);

        // 5. Render Active Tab Content
        if (activeTab.equals("Performance") && this.performanceTab != null) {
            this.performanceTab.render(context, mouseX, mouseY, delta);
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
