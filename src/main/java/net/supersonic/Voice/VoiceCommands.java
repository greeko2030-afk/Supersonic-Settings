package net.supersonic.voice;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.supersonic.SupersonicClient;
import net.supersonic.SupersonicConfig;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Decides whether a transcript is sent as chat or run as a server command. */
public final class VoiceCommands {
    private VoiceCommands() {
    }

    /** Only unambiguous command shapes are auto-detected, so normal chat is never turned into a command by accident. */
    private static final List<Pattern> AUTO = List.of(
            Pattern.compile("^gamemode (survival|creative|adventure|spectator)( .+)?$"),
            Pattern.compile("^difficulty (peaceful|easy|normal|hard)$"),
            Pattern.compile("^time (set|add|query) .+$"),
            Pattern.compile("^weather (clear|rain|thunder)( \\d+)?$"),
            Pattern.compile("^(tp|teleport) .+$"),
            Pattern.compile("^give .+$"),
            Pattern.compile("^summon .+$"),
            Pattern.compile("^(setblock|fill) .+$"),
            Pattern.compile("^gamerule \\w+( \\w+)?$"),
            Pattern.compile("^effect (give|clear) .+$"),
            Pattern.compile("^enchant .+$"),
            Pattern.compile("^(xp|experience) (add|set|query) .+$"),
            Pattern.compile("^kill @.+$"),
            Pattern.compile("^(setworldspawn|spawnpoint)( .+)?$"),
            Pattern.compile("^locate .+$"),
            Pattern.compile("^seed$")
    );

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

        String command = null;
        boolean canRunCommands = mc.isInSingleplayer() || player.hasPermissionLevel(2);
        if (cfg.voiceAutoCommands && canRunCommands) {
            command = detectCommand(text, cfg);
        }

        if (command != null && !command.isEmpty()) {
            net.sendChatCommand(command);
        } else {
            if (text.length() > 256) text = text.substring(0, 256);
            net.sendChatMessage(text);
        }
    }

    private static String detectCommand(String text, SupersonicConfig cfg) {
        String lower = text.toLowerCase(Locale.ROOT)
                .replaceAll("[.!?,।]+$", "")
                .trim();

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
        if (rest != null) return fixSelectors(rest);

        // Automatic: unambiguous command shapes
        String fixed = fixSelectors(lower);
        for (Pattern p : AUTO) {
            if (p.matcher(fixed).matches()) return fixed;
        }
        return null;
    }

    /** "at s" -> "@s", "at a" -> "@a" ... because speech has no @ sign. */
    private static String fixSelectors(String s) {
        return s.replaceAll("\\bat ([separ])\\b", "@$1");
    }
}
