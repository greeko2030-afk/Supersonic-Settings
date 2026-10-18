package net.supersonic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.supersonic.gui.SupersonicScreen;
import net.supersonic.hud.HudRenderer;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupersonicClient implements ClientModInitializer {
    public static final String MOD_ID = "supersonicsettings";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static SupersonicConfig CONFIG;

    private static boolean comboWasDown = false;

    @Override
    public void onInitializeClient() {
        CONFIG = SupersonicConfig.load();

        // Ctrl + Right Shift opens / closes the settings screen
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long window = client.getWindow().getHandle();
            boolean ctrl = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                    || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
            boolean rightShift = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
            boolean down = ctrl && rightShift;

            if (down && !comboWasDown) {
                toggleScreen(client);
            }
            comboWasDown = down;
        });

        // In-game HUD overlay
        HudRenderCallback.EVENT.register(HudRenderer::render);

        LOGGER.info("Supersonic Engine loaded. Press Ctrl + Right Shift to open settings.");
    }

    private static void toggleScreen(MinecraftClient client) {
        if (client.currentScreen instanceof SupersonicScreen) {
            client.setScreen(null);
        } else if (client.currentScreen == null) {
            client.setScreen(new SupersonicScreen(null));
        }
    }
}
