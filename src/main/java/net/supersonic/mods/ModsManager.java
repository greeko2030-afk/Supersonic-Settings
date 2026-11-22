package net.supersonic.mods;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Looks at the jar files in the mods folder and can switch them on/off.
 * A mod is switched off by renaming "x.jar" to "x.jar.disabled" (Fabric only loads *.jar files).
 * Changes take effect after the game is restarted.
 */
public final class ModsManager {
    /** Set to true whenever the mods list on screen has to be rebuilt (read by the GUI on the render thread). */
    public static volatile boolean dirty = true;

    public static final class Entry {
        public final String key;          // file name without ".disabled", for example "sodium.jar"
        public volatile File file;        // current file on disk
        public String id = "";
        public String name;
        public String version = "?";
        public boolean self = false;      // this very mod: it must not disable itself
        // Windows only: the rename could not be done now (file is in use), it is done when the game closes
        volatile Boolean target = null;
        volatile File[] queued = null;

        Entry(String key, File file) {
            this.key = key;
            this.file = file;
            this.name = key.endsWith(".jar") ? key.substring(0, key.length() - 4) : key;
        }

        public boolean enabled() {
            Boolean t = target;
            return t != null ? t : file.getName().endsWith(".jar");
        }

        public String fileName() {
            return key;
        }
    }

    private static final Map<String, Entry> CACHE = new HashMap<>();
    private static final Set<String> CHANGED = new HashSet<>();    // keys toggled this session
    private static final Set<String> INSTALLED = new HashSet<>();  // files downloaded this session
    private static final List<File[]> WIN_QUEUE = new ArrayList<>();
    private static boolean hookAdded = false;
    private static volatile Set<String> ids = new HashSet<>();

    private ModsManager() {
    }

    public static File modsDir() {
        File d = FabricLoader.getInstance().getGameDir().resolve("mods").toFile();
        d.mkdirs();
        return d;
    }

    /** Lists every mod jar (on or off), sorted by name. */
    public static synchronized List<Entry> scan() {
        List<Entry> list = new ArrayList<>();
        File[] files = modsDir().listFiles((d, n) -> n.endsWith(".jar") || n.endsWith(".jar.disabled"));
        Set<String> seen = new HashSet<>();
        Set<String> found = new HashSet<>();
        if (files != null) {
            for (File f : files) {
                if (!f.isFile()) continue;
                String n = f.getName();
                String key = n.endsWith(".disabled") ? n.substring(0, n.length() - ".disabled".length()) : n;
                if (!seen.add(key)) continue; // both x.jar and x.jar.disabled exist: show it once
                Entry e = CACHE.get(key);
                if (e == null) {
                    e = new Entry(key, f);
                    readMeta(e, f);
                    CACHE.put(key, e);
                }
                if (e.queued == null) e.file = f;
                list.add(e);
                if (!e.id.isEmpty()) found.add(e.id.toLowerCase(Locale.ROOT));
            }
        }
        ids = found;
        list.sort(Comparator.comparing(e -> e.name.toLowerCase(Locale.ROOT)));
        return list;
    }

    /** True when a mod with this id is loaded or sits in the mods folder (uses the list from the last scan). */
    public static boolean hasId(String id) {
        if (id == null || id.isEmpty()) return false;
        String a = id.toLowerCase(Locale.ROOT);
        String b = a.replace('-', '_');
        FabricLoader fl = FabricLoader.getInstance();
        if (fl.isModLoaded(a) || fl.isModLoaded(b)) return true;
        Set<String> s = ids;
        return s.contains(a) || s.contains(b);
    }

    private static void readMeta(Entry e, File f) {
        JsonObject o = readModJson(f);
        if (o == null) return;
        e.id = str(o, "id", "");
        e.name = str(o, "name", e.name);
        e.version = str(o, "version", "?");
        e.self = e.id.equals("supersonicsettings");
    }

    /** Reads the mod id from a jar. Empty when the jar has no fabric.mod.json. */
    public static String readId(File jar) {
        JsonObject o = readModJson(jar);
        return o == null ? "" : str(o, "id", "");
    }

    private static JsonObject readModJson(File f) {
        try (ZipFile z = new ZipFile(f)) {
            ZipEntry ze = z.getEntry("fabric.mod.json");
            if (ze == null) return null;
            try (InputStreamReader r = new InputStreamReader(z.getInputStream(ze), StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(r).getAsJsonObject();
            }
        } catch (Throwable t) {
            return null;
        }
    }

    private static String str(JsonObject o, String key, String def) {
        try {
            if (o.has(key) && !o.get(key).isJsonNull()) {
                String s = o.get(key).getAsString();
                if (!s.isEmpty()) return s;
            }
        } catch (Throwable ignored) {
            // wrong type in the json: use the default
        }
        return def;
    }

    /** Switches a mod on or off. Returns an error text, or null when it worked. */
    public static synchronized String setEnabled(Entry e, boolean enable) {
        if (e.self) return "Supersonic Settings cannot switch itself off.";
        if (e.enabled() == enable) return null;

        if (e.queued != null) { // a delayed Windows rename exists: toggling back just cancels it
            WIN_QUEUE.remove(e.queued);
            e.queued = null;
            e.target = null;
            flipChanged(e.key);
            return null;
        }

        File from = e.file;
        File to = new File(from.getParentFile(), enable ? e.key : e.key + ".disabled");
        try {
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
            e.file = to;
            flipChanged(e.key);
            return null;
        } catch (Throwable ex) {
            if (isWindows()) {
                // Windows keeps loaded jars locked: do the rename right after the game has closed
                File[] op = {from, to};
                WIN_QUEUE.add(op);
                e.queued = op;
                e.target = enable;
                flipChanged(e.key);
                ensureHook();
                return null;
            }
            return "Could not rename the file: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    private static void flipChanged(String key) {
        if (!CHANGED.remove(key)) CHANGED.add(key);
        dirty = true;
    }

    static synchronized void markInstalled(String fileName) {
        INSTALLED.add(fileName);
        dirty = true;
    }

    /** How many changes only take effect after a restart. */
    public static synchronized int pendingChanges() {
        return CHANGED.size() + INSTALLED.size();
    }

    // ---------------------------------------------------------------- Windows helper

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static void ensureHook() {
        if (hookAdded) return;
        hookAdded = true;
        Runtime.getRuntime().addShutdownHook(new Thread(ModsManager::runWindowsHelper, "Supersonic-Mods-Rename"));
    }

    /** Starts a small batch file that waits until the game has exited, then does the queued renames. */
    private static void runWindowsHelper() {
        try {
            List<File[]> ops;
            synchronized (ModsManager.class) {
                ops = new ArrayList<>(WIN_QUEUE);
            }
            if (ops.isEmpty()) return;
            File bat = new File(System.getProperty("java.io.tmpdir"), "supersonic-mods-apply.bat");
            StringBuilder sb = new StringBuilder("@echo off\r\nping -n 6 127.0.0.1 >nul\r\n");
            for (File[] op : ops) {
                sb.append("move /y \"").append(op[0].getAbsolutePath()).append("\" \"")
                        .append(op[1].getAbsolutePath()).append("\"\r\n");
            }
            sb.append("del \"%~f0\"\r\n");
            Files.write(bat.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            new ProcessBuilder("cmd.exe", "/c", "start", "", "/min", bat.getAbsolutePath()).start();
        } catch (Throwable ignored) {
            // nothing more can be done while the game is closing
        }
    }
}
