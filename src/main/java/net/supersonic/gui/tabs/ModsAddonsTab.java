package net.supersonic.gui.tabs;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.supersonic.gui.widgets.SupersonicToggle;
import java.util.List;
import java.util.ArrayList;

public class ModsAddonsTab {
    private final int x, y;
    private final List<ModContainer> activeMods;

    public ModsAddonsTab(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        // Fetching REAL loaded mods using Fabric API
        this.activeMods = new ArrayList<>(FabricLoader.getInstance().getAllMods());
    }

    public void init(List<ClickableWidget> widgets, MinecraftClient client) {
        int contentX = x + 200;
        int toggleX = contentX + 380;
        int currentY = y + 100;

        // Create a visual toggle for the first 5 mods as a demonstration
        int maxDisplay = Math.min(activeMods.size(), 5);
        for (int i = 0; i < maxDisplay; i++) {
            // Note: Fabric doesn't allow disabling mods at runtime natively. 
            // This toggle would theoretically update a custom JSON to disable the mod on next launch.
            widgets.add(new SupersonicToggle(toggleX, currentY, 40, 16, true, state -> {
                // Logic to save disabled state to a mod manager config
            }));
            currentY += 40;
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int contentX = x + 200;

        context.drawText(client.textRenderer, "MODS & ADDONS", contentX, y + 20, 0xFFFFFF, true);
        context.drawText(client.textRenderer, "ACTIVE MODS (" + activeMods.size() + ")", contentX, y + 75, 0x00FFFF, false);

        int currentY = y + 100;
        int maxDisplay = Math.min(activeMods.size(), 5);

        // Rendering the REAL mod names and versions
        for (int i = 0; i < maxDisplay; i++) {
            ModContainer mod = activeMods.get(i);
            String modName = mod.getMetadata().getName();
            String modVersion = mod.getMetadata().getVersion().getFriendlyString();

            context.drawText(client.textRenderer, modName, contentX + 30, currentY, 0xFFFFFF, false);
            context.drawText(client.textRenderer, "v" + modVersion, contentX + 320, currentY, 0xAAAAAA, false);
            
            currentY += 40;
        }
        
        // Draw Mod Details Right Panel
        drawModDetailsPanel(context, x + 720, y + 20);
    }
    
    private void drawModDetailsPanel(DrawContext context, int panelX, int panelY) {
        context.fill(panelX, panelY, panelX + 220, panelY + 480, 0xFF0A111A);
        context.drawBorder(panelX, panelY, 220, 480, 0xFF1A3344);
        context.drawText(MinecraftClient.getInstance().textRenderer, "MOD DETAILS", panelX + 15, panelY + 15, 0x00FFFF, false);
    }
}
