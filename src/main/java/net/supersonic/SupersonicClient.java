package net.supersonic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.supersonic.gui.CaptureTools;
import net.supersonic.gui.SupersonicScreen;
import net.supersonic.hud.HudRenderer;
import net.supersonic.voice.ModelManager;
import net.supersonic.voice.VoiceManager;
import net.supersonic.voice.VoiceOverlay;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupersonicClient implements ClientModInitializer {
    public static final String MOD_ID = "supersonicsettings";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static SupersonicConfig CONFIG;

    /** Last time (System.nanoTime) the F3 screen was drawn; set by DebugHudMixin. -1 = never. */
    public static volatile long lastF3Draw = -1L;

    private static boolean comboWasDown = false;
    private static KeyBinding voiceKey;
    private static volatile long lastShiftNs = Long.MIN_VALUE / 2;

    /** Remembers when Left Shift was last seen down (called every frame, so short taps are not missed). */
    public static void sampleKeys() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null) return;
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            lastShiftNs = System.nanoTime();
        }
    }

    @Override
    public void onInitializeClient() {
        CONFIG = SupersonicConfig.load();
        VoiceManager.prefetchLanguages();

        // Real key binding (event based): shows up in Controls and catches very short presses
        voiceKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.supersonicsettings.voice", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V,
                "key.categories.supersonicsettings"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Ctrl + Right Shift opens / closes the settings screen
            long window = client.getWindow().getHandle();
            boolean ctrl = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                    || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
            boolean rightShift = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
            boolean down = ctrl && rightShift;

            if (down && !comboWasDown) {
                toggleScreen(client);
            }
            comboWasDown = down;

            // Left Shift + V toggles the microphone (speech-to-text)
            while (voiceKey.wasPressed()) {
                if (!CONFIG.voiceEnabled) {
                    VoiceManager.notifyPlayer("The voice hotkey is turned off in Supersonic Settings > Voice Chat.");
                    continue;
                }
                boolean shiftOk = !CONFIG.voiceRequireShift
                        || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                        || System.nanoTime() - lastShiftNs < 500_000_000L;
                if (shiftOk && client.player != null) {
                    VoiceManager.toggle();
                }
            }

            // Leaving the world turns the microphone off
            if (client.player == null && VoiceManager.isOn()) {
                VoiceManager.stop();
            }

            CaptureTools.tick(client);
            smartSprint(client);
        });

        // Download missing speech files in the background as soon as the player joins a world
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ModelManager.autoDownloadIfNeeded());

        HudRenderCallback.EVENT.register(HudRenderer::render);
        HudRenderCallback.EVENT.register(VoiceOverlay::render);

        LOGGER.info("Supersonic Engine loaded. Press Ctrl + Right Shift to open settings.");
    }

    private static void toggleScreen(MinecraftClient client) {
        if (client.currentScreen instanceof SupersonicScreen) {
            client.setScreen(null);
        } else if (client.currentScreen == null) {
            client.setScreen(new SupersonicScreen(null));
        }
    }

    /** Sprints automatically while holding forward (when it is safe to do so). */
    private static void smartSprint(MinecraftClient client) {
        if (CONFIG == null || !CONFIG.smartSprint) return;
        ClientPlayerEntity p = client.player;
        if (p == null || client.currentScreen != null) return;

        if (client.options.forwardKey.isPressed()
                && !p.isSneaking()
                && !p.horizontalCollision
                && !p.isUsingItem()
                && p.getHungerManager().getFoodLevel() > 6
                && !p.hasStatusEffect(StatusEffects.BLINDNESS)) {
            p.setSprinting(true);
        }
    }
}
