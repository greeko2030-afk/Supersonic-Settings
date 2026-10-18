package net.supersonic.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.supersonic.SupersonicClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Adds the "Supersonic Engine" block to the left side of the F3 debug screen. */
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void supersonic$addEngineInfo(CallbackInfoReturnable<List<String>> cir) {
        if (SupersonicClient.CONFIG == null || !SupersonicClient.CONFIG.f3Info) return;

        List<String> list = cir.getReturnValue();
        MinecraftClient mc = MinecraftClient.getInstance();

        String mcVersion = FabricLoader.getInstance().getModContainer("minecraft")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("?");

        int fps = Math.max(1, mc.getCurrentFps());
        int maxFps = mc.options.getMaxFps().getValue();
        String limit = maxFps >= 260 ? "Unlimited" : maxFps + " FPS";
        int mods = FabricLoader.getInstance().getAllMods().size();

        List<String> add = new ArrayList<>();
        add.add("§bSupersonic Engine§r (mc" + mcVersion + ")");
        add.add(String.format(Locale.ROOT, "Frame: %.2f ms | Limit: %s", 1000.0 / fps, limit));
        add.add("Dist: R" + mc.options.getViewDistance().getValue()
                + " / S" + mc.options.getSimulationDistance().getValue() + " chunks");
        add.add("Mods: " + mods + " loaded | Menu: Ctrl+RShift");
        add.add("");

        // Insert after the first block (the first empty line), like Sodium/Iris do
        int idx = list.indexOf("");
        idx = idx < 0 ? list.size() : idx + 1;
        list.addAll(idx, add);
    }
}
