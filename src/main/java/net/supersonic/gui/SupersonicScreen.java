package net.supersonic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.supersonic.gui.SupersonicScreen;
import org.lwjgl.glfw.GLFW;

public class SupersonicClient implements ClientModInitializer {
    
    private static KeyBinding openSettingsKey;

    @Override
    public void onInitializeClient() {
        // Registering the Right Shift key
        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.supersonic.open_menu", 
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, // RIGHT SHIFT KEY
                "category.supersonic.main"
        ));

        // Checking for key press every tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.wasPressed()) {
                // Open the screen if the player is in-game
                if (client.player != null) {
                    client.setScreen(new SupersonicScreen());
                }
            }
        });
    }
}
