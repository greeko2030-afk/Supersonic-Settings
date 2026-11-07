package net.supersonic.voice;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;

import java.util.List;
import java.util.Locale;

/** Decides whether a transcript is sent as chat or run as server command(s). */
public final class VoiceCommands {
    private VoiceCommands() {
    }

    /** Call on the main client thread. */
    public static void handle(String raw) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayNetworkHandler net = mc.getNetworkHandler();
        ClientPlayerEntity player = mc.player;
        SupersonicConfig cfg = SupersonicClient.CONFIG;
        if (net == null || player == null || raw == null) return;

        String text = raw.replace('\n', ' ').trim();
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        if (text.isEmpty() || text.toLowerCase(Locale.ROOT).contains("[silence]")) return;

        VoiceManager.setHeard(text);

        List<String> commands = null;
        boolean canRunCommands = mc.isInSingleplayer() || player.hasPermissionLevel(2);
        if (cfg.voiceAutoCommands && canRunCommands) {
            commands = detectCommands(text, cfg);
        }

        if (commands != null && !commands.isEmpty()) {
            int sent = 0;
            for (String c : commands) {
                if (sent >= 10 || c.isEmpty()) break;
                net.sendChatCommand(c);
                sent++;
            }
            String shown = commands.get(0) + (commands.size() > 1 ? "  (x" + commands.size() + ")" : "");
            VoiceManager.notifyPlayer("§7Command: /" + shown);
        } else {
            if (text.length() > 256) text = text.substring(0, 256);
            net.sendChatMessage(text);
        }
    }

    private static List<String> detectCommands(String text, SupersonicConfig cfg) {
        String lower = text.toLowerCase(Locale.ROOT).replaceAll("[.!?,।]+$", "").trim();

        // Explicit: "/time set day", "slash time set day", "command time set day"
        String rest = null;
        if (lower.startsWith("/")) {
            rest = lower.substring(1).trim();
        } else if (lower.startsWith("slash ")) {
            rest = lower.substring(6).trim();
        } else {
            String word = cfg.voiceCommandWord == null ? "" : cfg.voiceCommandWord.trim().toLowerCase(Locale.ROOT);
            if (!word.isEmpty() && lower.startsWith(word + " ")) {
                rest = lower.substring(word.length() + 1).trim();
            }
        }

        if (rest != null) {
            List<String> parsed = SpokenCommands.parse(rest);
            if (parsed != null) return parsed;
            // The player said "command ..." but it is not a known phrase: run it as spoken.
            return List.of(rest.replaceAll("\\bat (?:s|self|me)\\b", "@s")
                    .replaceAll("\\bat (?:a|all|everyone)\\b", "@a")
                    .replaceAll("\\bat (?:p|nearest)\\b", "@p")
                    .replaceAll("\\bat (?:e|entities)\\b", "@e")
                    .replaceAll("\\bat (?:r|random)\\b", "@r"));
        }

        // Automatic: only sentences that match a known command phrase
        return SpokenCommands.parse(lower);
    }
}
