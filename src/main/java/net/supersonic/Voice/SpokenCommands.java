package net.supersonic.voice;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns natural spoken sentences into Minecraft commands.
 * Handles number words ("one hundred twenty"), filler words, common mis-hearings ("whether" -> "weather"),
 * plural item names and checks item / mob / effect names against the game's own registries.
 */
public final class SpokenCommands {
    private SpokenCommands() {
    }

    private record Rule(Pattern pattern, Function<Matcher, List<String>> action) {
    }

    private static final List<Rule> RULES = new ArrayList<>();

    private static final String MODES = "survival|creative|adventure|spectator";
    private static final String WEATHER = "clear|sunny|rain|rainy|raining|thunder|storm|stormy|thunderstorm";

    private static final Map<String, String> ITEM_ALIAS = Map.of(
            "steak", "cooked_beef",
            "tnt", "tnt",
            "gapple", "golden_apple",
            "god apple", "enchanted_golden_apple",
            "xp bottle", "experience_bottle",
            "bottle of xp", "experience_bottle");

    private static final Map<String, String> ENTITY_ALIAS = Map.of(
            "zombie pigman", "zombified_piglin",
            "pigman", "zombified_piglin",
            "iron golem", "iron_golem",
            "snow golem", "snow_golem",
            "dragon", "ender_dragon");

    private static final Map<String, String> EFFECT_ALIAS = Map.of(
            "jump", "jump_boost",
            "fire resist", "fire_resistance",
            "heal", "instant_health",
            "health", "instant_health",
            "damage", "instant_damage");

    private static final Set<String> ENCHANTS = Set.of(
            "sharpness", "smite", "bane_of_arthropods", "knockback", "fire_aspect", "looting", "sweeping_edge",
            "efficiency", "silk_touch", "unbreaking", "fortune", "power", "punch", "flame", "infinity",
            "luck_of_the_sea", "lure", "loyalty", "riptide", "channeling", "impaling", "mending", "protection",
            "fire_protection", "feather_falling", "blast_protection", "projectile_protection", "respiration",
            "aqua_affinity", "thorns", "depth_strider", "frost_walker", "soul_speed", "swift_sneak", "multishot",
            "piercing", "quick_charge", "density", "breach", "wind_burst", "binding_curse", "vanishing_curse");

    private static final Map<String, String> GAMERULES = Map.ofEntries(
            Map.entry("keep inventory", "keepInventory"),
            Map.entry("mob griefing", "mobGriefing"),
            Map.entry("daylight cycle", "doDaylightCycle"),
            Map.entry("day night cycle", "doDaylightCycle"),
            Map.entry("weather cycle", "doWeatherCycle"),
            Map.entry("fire spread", "doFireTick"),
            Map.entry("mob spawning", "doMobSpawning"),
            Map.entry("immediate respawn", "doImmediateRespawn"),
            Map.entry("command feedback", "sendCommandFeedback"),
            Map.entry("insomnia", "doInsomnia"),
            Map.entry("mob loot", "doMobLoot"),
            Map.entry("tile drops", "doTileDrops"),
            Map.entry("fall damage", "fallDamage"),
            Map.entry("fire damage", "fireDamage"),
            Map.entry("drowning damage", "drowningDamage"));

    private static final String[] VERBS = {"kill", "gamemode", "time", "weather", "difficulty", "teleport", "give",
            "summon", "spawn", "effect", "enchant", "clear", "heal", "feed", "switch", "change", "enable", "disable"};

    static {
        // ---- kill
        add("^(?:kill|suicide)(?: me| myself| self)?$", m -> one("kill"));
        add("^kill (?:everyone|everybody|all players)$", m -> one("kill @a"));
        add("^kill (?:all )?(?:mobs|monsters|entities|everything)$", m -> one("kill @e[type=!player]"));
        add("^kill (?:all )?(?:the )?([a-z ]+)$", m -> {
            String id = entityId(m.group(1));
            return id == null ? null : one("kill @e[type=minecraft:" + id + "]");
        });

        // ---- gamemode
        add("^(?:set |change |switch )?(?:my |the )?gamemode (?:to |into )?(" + MODES + ")$", m -> one("gamemode " + m.group(1)));
        add("^(?:switch to|change to|go to|set to|set|make me|put me in|change my mode to|switch me to) ("
                + MODES + ")(?: mode)?$", m -> one("gamemode " + m.group(1)));
        add("^(" + MODES + ") mode$", m -> one("gamemode " + m.group(1)));

        // ---- time
        add("^(?:set |change )?(?:the )?time (?:set )?(?:to |at )?(day|night|noon|midnight|sunrise|sunset)$", m -> one("time set " + m.group(1)));
        add("^(?:set |change )?(?:the )?time (?:set )?(?:to |at )?(\\d+)$", m -> one("time set " + m.group(1)));
        add("^(?:make it|set it to|change it to|turn it to|turn it into|change to|skip to|set) "
                + "(day|night|noon|midnight|daytime|nighttime|morning|evening)$", m -> one("time set " + timeWord(m.group(1))));

        // ---- weather
        add("^(?:set |change )?(?:the )?weather (?:to )?(" + WEATHER + ")$", m -> one("weather " + weather(m.group(1))));
        add("^(?:make it|set it to|change it to|turn it to|make the weather) (" + WEATHER + ")$", m -> one("weather " + weather(m.group(1))));
        add("^(?:stop|end|clear|remove) (?:the )?(?:rain|storm|weather|thunder|thunderstorm)$", m -> one("weather clear"));
        add("^(?:start|begin|make) (?:the )?(rain|storm|thunder)$", m -> one("weather " + weather(m.group(1))));

        // ---- difficulty
        add("^(?:set |change )?(?:the )?difficulty (?:to )?(peaceful|easy|normal|hard)$", m -> one("difficulty " + m.group(1)));
        add("^(?:set it to|make it|change it to) (peaceful|easy|normal|hard)(?: mode)?$", m -> one("difficulty " + m.group(1)));

        // ---- teleport
        add("^(?:teleport|tp)(?: me)?(?: to)? (-?\\d+) (-?\\d+) (-?\\d+)$",
                m -> one("tp @s " + m.group(1) + " " + m.group(2) + " " + m.group(3)));
        add("^(?:teleport|tp)(?: me)? to ([a-z0-9_ ]+)$", m -> {
            String p = player(m.group(1));
            return p == null ? null : one("tp @s " + p);
        });
        add("^(?:teleport|tp) ([a-z0-9_ ]+) to me$", m -> {
            String p = player(m.group(1));
            return p == null ? null : one("tp " + p + " @s");
        });

        // ---- xp
        add("^(?:give me |add )?(\\d+) (?:xp |experience )?levels?$", m -> one("xp add @s " + m.group(1) + " levels"));
        add("^(?:give me |add )?(\\d+) (?:xp|experience|points?|xp points?|experience points?)$",
                m -> one("xp add @s " + m.group(1) + " points"));

        // ---- inventory / effects / spawn / health
        add("^clear (?:my )?inventory$", m -> one("clear @s"));
        add("^(?:clear|remove|stop) (?:all )?(?:my )?(?:effects?|potions?)$", m -> one("effect clear @s"));
        add("^set (?:my )?spawn(?: point)?$", m -> one("spawnpoint @s"));
        add("^set world spawn$", m -> one("setworldspawn"));
        add("^(?:heal|heal me|full health|restore health|restore my health)$",
                m -> one("effect give @s minecraft:instant_health 1 10 true"));
        add("^(?:feed|feed me|fill my hunger|full hunger|restore hunger)$",
                m -> one("effect give @s minecraft:saturation 1 10 true"));
        add("^(?:show |what is the |tell me the )?(?:world )?seed$", m -> one("seed"));

        // ---- effect
        add("^effect (?:give )?(?:me )?([a-z ]+?)(?: (\\d+))?(?: (\\d+))?$", m -> effect(m.group(1), m.group(2), m.group(3)));

        // ---- enchant
        add("^enchant (?:my |the )?(?:item |held item |sword |tool |pickaxe |armor |bow )?(?:with )?([a-z ]+?)(?: (\\d+))?$", m -> {
            String id = lookup(m.group(1), ENCHANTS::contains, Map.of());
            if (id == null) return null;
            int level = m.group(2) == null ? 1 : Math.max(1, Integer.parseInt(m.group(2)));
            return one("enchant @s minecraft:" + id + " " + level);
        });

        // ---- summon / spawn
        add("^(?:summon|spawn)(?: me)?(?: (\\d+))?(?: (?:a|an|some))? ([a-z ]+)$", m -> {
            String id = entityId(m.group(2));
            if (id == null) return null;
            int count = m.group(1) == null ? 1 : Math.max(1, Math.min(10, Integer.parseInt(m.group(1))));
            List<String> out = new ArrayList<>();
            for (int i = 0; i < count; i++) out.add("summon minecraft:" + id);
            return out;
        });

        // ---- give (item, or an effect if the name is an effect)
        add("^give (?:me )?(.+)$", m -> give(m.group(1)));
    }

    private static void add(String regex, Function<Matcher, List<String>> action) {
        RULES.add(new Rule(Pattern.compile(regex), action));
    }

    private static List<String> one(String command) {
        return List.of(command);
    }

    // ------------------------------------------------------------------ API

    /** Returns the commands (without the leading slash) for a spoken sentence, or null if it is not a command. */
    public static List<String> parse(String spoken) {
        String t = normalize(spoken);
        if (t.isEmpty()) return null;
        for (Rule r : RULES) {
            Matcher m = r.pattern().matcher(t);
            if (m.matches()) {
                try {
                    List<String> out = r.action().apply(m);
                    if (out != null && !out.isEmpty()) return out;
                } catch (RuntimeException ignored) {
                    // a bad number or similar: treat as "not a command"
                }
            }
        }
        return gamerule(t);
    }

    // ---------------------------------------------------------- normalizing

    static String normalize(String spoken) {
        String t = spoken.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9@\\- ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // common mis-hearings and spacing
        t = t.replace("game mode", "gamemode")
                .replace("whether", "weather")
                .replace("wether", "weather")
                .replaceAll("\\bt p\\b", "tp")
                .replaceAll("\\bteleported\\b", "teleport");

        // polite / filler words at the start and end
        Pattern lead = Pattern.compile("^(?:please|hey|ok|okay|now|just|can you|could you|would you|will you|"
                + "i want to|i want|i need to|i need|let me|let us|lets|let s|i would like to|go ahead and) ");
        Matcher m = lead.matcher(t);
        while (m.find()) {
            t = t.substring(m.end());
            m = lead.matcher(t);
        }
        t = t.replaceAll(" (?:please|now|thanks|thank you)$", "");

        t = numbers(t);

        // selectors
        t = t.replaceAll("\\bat (?:s|self|me)\\b", "@s")
                .replaceAll("\\bat (?:a|all|everyone)\\b", "@a")
                .replaceAll("\\bat (?:p|nearest)\\b", "@p")
                .replaceAll("\\bat (?:e|entities)\\b", "@e")
                .replaceAll("\\bat (?:r|random)\\b", "@r");

        // first word: fix a single typo (for example "wether" -> "weather")
        String[] parts = t.split(" ", 2);
        String v = parts[0];
        if (v.length() >= 5 && !Arrays.asList(VERBS).contains(v)) {
            for (String c : VERBS) {
                if (distance(v, c) == 1) {
                    parts[0] = c;
                    break;
                }
            }
            t = String.join(" ", parts);
        }
        return t.trim();
    }

    private static int distance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[b.length()];
    }

    // ------------------------------------------------------- number words

    private static final int ZERO = 0, ONE = 1, TEEN = 2, TENS = 3, HUNDRED = 4, THOUSAND = 5;

    private static final Map<String, Integer> VALUES = Map.ofEntries(
            Map.entry("zero", 0), Map.entry("one", 1), Map.entry("two", 2), Map.entry("three", 3),
            Map.entry("four", 4), Map.entry("five", 5), Map.entry("six", 6), Map.entry("seven", 7),
            Map.entry("eight", 8), Map.entry("nine", 9), Map.entry("ten", 10), Map.entry("eleven", 11),
            Map.entry("twelve", 12), Map.entry("thirteen", 13), Map.entry("fourteen", 14), Map.entry("fifteen", 15),
            Map.entry("sixteen", 16), Map.entry("seventeen", 17), Map.entry("eighteen", 18), Map.entry("nineteen", 19),
            Map.entry("twenty", 20), Map.entry("thirty", 30), Map.entry("forty", 40), Map.entry("fifty", 50),
            Map.entry("sixty", 60), Map.entry("seventy", 70), Map.entry("eighty", 80), Map.entry("ninety", 90));

    private static int typeOf(String w) {
        if (w.equals("hundred")) return HUNDRED;
        if (w.equals("thousand")) return THOUSAND;
        Integer v = VALUES.get(w);
        if (v == null) return -1;
        if (v == 0) return ZERO;
        if (v < 10) return ONE;
        if (v < 20) return TEEN;
        return TENS;
    }

    private static boolean allowed(int prev, int type) {
        return switch (prev) {
            case -1 -> true;
            case ONE, TEEN -> type == HUNDRED || type == THOUSAND;
            case TENS -> type == ONE || type == THOUSAND;
            case HUNDRED -> type == ONE || type == TEEN || type == TENS || type == THOUSAND;
            case THOUSAND -> type == ONE || type == TEEN || type == TENS || type == HUNDRED;
            default -> false;
        };
    }

    /** "teleport to one hundred twenty five minus sixty four" -> "teleport to 125 -64". */
    private static String numbers(String t) {
        String[] tok = t.split(" ");
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < tok.length) {
            boolean neg = false;
            if ((tok[i].equals("minus") || tok[i].equals("negative")) && i + 1 < tok.length && typeOf(tok[i + 1]) >= 0) {
                neg = true;
                i++;
            }
            if (typeOf(tok[i]) >= 0) {
                long total = 0L;
                long cur = 0L;
                int prev = -1;
                int j = i;
                while (j < tok.length) {
                    String w = tok[j];
                    int type = typeOf(w);
                    if (type < 0) {
                        if (w.equals("and") && prev >= HUNDRED && j + 1 < tok.length && typeOf(tok[j + 1]) >= 0) {
                            j++;
                            continue;
                        }
                        break;
                    }
                    if (!allowed(prev, type)) break;
                    if (type == HUNDRED) {
                        cur = (cur == 0 ? 1 : cur) * 100;
                    } else if (type == THOUSAND) {
                        total += (cur == 0 ? 1 : cur) * 1000;
                        cur = 0;
                    } else {
                        cur += VALUES.get(w);
                    }
                    prev = type;
                    j++;
                }
                out.append(neg ? "-" : "").append(total + cur).append(' ');
                i = j;
            } else {
                out.append(tok[i]).append(' ');
                i++;
            }
        }
        return out.toString().trim();
    }

    // -------------------------------------------------------------- lookups

    /** Finds a valid id for spoken words, trying plural forms and aliases. */
    private static String lookup(String words, Predicate<String> exists, Map<String, String> alias) {
        String base = words.trim().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");
        if (base.startsWith("the ")) base = base.substring(4);
        if (base.isEmpty()) return null;
        if (alias.containsKey(base)) return alias.get(base);

        List<String> candidates = new ArrayList<>();
        String id = base.replace(' ', '_');
        candidates.add(id);
        if (id.endsWith("ies")) candidates.add(id.substring(0, id.length() - 3) + "y");
        if (id.endsWith("es")) candidates.add(id.substring(0, id.length() - 2));
        if (id.endsWith("s")) candidates.add(id.substring(0, id.length() - 1));
        for (String c : candidates) {
            if (!c.isEmpty() && exists.test(c)) return c;
        }
        return null;
    }

    private static String itemId(String words) {
        return lookup(words, id -> Registries.ITEM.containsId(Identifier.of("minecraft", id)), ITEM_ALIAS);
    }

    private static String entityId(String words) {
        return lookup(words, id -> Registries.ENTITY_TYPE.containsId(Identifier.of("minecraft", id)), ENTITY_ALIAS);
    }

    private static String effectId(String words) {
        return lookup(words, id -> Registries.STATUS_EFFECT.containsId(Identifier.of("minecraft", id)), EFFECT_ALIAS);
    }

    /** Matches a spoken name against the players currently online. */
    private static String player(String spoken) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) return null;
        String want = spoken.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (want.length() < 2) return null;
        String partial = null;
        int partialCount = 0;
        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
            String name = e.getProfile().getName();
            String n = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
            if (n.equals(want)) return name;
            if (want.length() >= 3 && (n.startsWith(want) || n.contains(want))) {
                partial = name;
                partialCount++;
            }
        }
        return partialCount == 1 ? partial : null;
    }

    // -------------------------------------------------------------- helpers

    private static String timeWord(String w) {
        return switch (w) {
            case "daytime", "morning" -> "day";
            case "nighttime", "evening" -> "night";
            default -> w;
        };
    }

    private static String weather(String w) {
        if (w.equals("clear") || w.equals("sunny")) return "clear";
        if (w.contains("rain")) return "rain";
        return "thunder";
    }

    private static List<String> effect(String name, String a, String b) {
        String id = effectId(name);
        if (id == null) return null;
        int amplifier = a == null ? 0 : Math.max(0, Integer.parseInt(a) - 1); // "speed two" = level II
        int seconds = b == null ? 600 : Math.max(1, Integer.parseInt(b));
        return one("effect give @s minecraft:" + id + " " + seconds + " " + amplifier);
    }

    private static List<String> give(String rest) {
        String r = rest.trim();
        int count = 1;
        Matcher m;
        if ((m = Pattern.compile("^(\\d+) stacks? of (.+)$").matcher(r)).matches()) {
            count = Integer.parseInt(m.group(1)) * 64;
            r = m.group(2);
        } else if ((m = Pattern.compile("^(?:a )?stack of (.+)$").matcher(r)).matches()) {
            count = 64;
            r = m.group(1);
        } else if ((m = Pattern.compile("^(\\d+) (.+)$").matcher(r)).matches()) {
            count = Integer.parseInt(m.group(1));
            r = m.group(2);
        } else if ((m = Pattern.compile("^(?:a|an|one|some) (.+)$").matcher(r)).matches()) {
            r = m.group(1);
        }
        count = Math.max(1, Math.min(count, 2304));

        String item = itemId(r);
        if (item != null) return one("give @s minecraft:" + item + " " + count);

        // "give me night vision" -> an effect instead of an item
        String effect = effectId(rest.trim());
        if (effect != null) return one("effect give @s minecraft:" + effect + " 600 0");
        return null;
    }

    private static List<String> gamerule(String t) {
        for (Map.Entry<String, String> e : GAMERULES.entrySet()) {
            String key = e.getKey();
            if (!t.contains(key)) continue;
            String leftover = t.replace(key, " ")
                    .replaceAll("\\b(?:turn|switch|set|enable|disable|on|off|true|false|the|keep|stop|start|rule|game)\\b", " ")
                    .trim();
            if (!leftover.isEmpty()) continue;

            Boolean value = null;
            if (t.matches(".*\\b(?:off|disable|disabled|false|stop)\\b.*")) {
                value = Boolean.FALSE;
            } else if (t.matches(".*\\b(?:on|enable|enabled|true|start)\\b.*") || t.startsWith("keep ")) {
                value = Boolean.TRUE;
            }
            if (value != null) return one("gamerule " + e.getValue() + " " + value);
        }
        return null;
    }
}
