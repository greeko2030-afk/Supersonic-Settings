package net.supersonic.mods;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Searches Modrinth (modrinth.com) for Fabric mods and installs them, including required dependencies. */
public final class ModrinthBrowser {
    private static final String API = "https://api.modrinth.com/v2";
    private static final String UA = "SupersonicSettings/1.0 (Minecraft Fabric mod)";

    public enum Sort {
        DOWNLOADS("downloads", "Most Downloaded"),
        RELEVANCE("relevance", "Relevance"),
        NEWEST("newest", "Newest"),
        UPDATED("updated", "Recently Updated");

        final String api;
        public final String label;

        Sort(String api, String label) {
            this.api = api;
            this.label = label;
        }
    }

    public static final class Hit {
        public final String id;
        public final String slug;
        public final String title;
        public final String description;
        public final String author;
        public final long downloads;

        Hit(String id, String slug, String title, String description, String author, long downloads) {
            this.id = id;
            this.slug = slug;
            this.title = title;
            this.description = description;
            this.author = author;
            this.downloads = downloads;
        }
    }

    private static final class AlreadyInstalled extends IOException {
        AlreadyInstalled(String m) {
            super(m);
        }
    }

    public static volatile String query = "";
    public static volatile Sort sort = Sort.DOWNLOADS;
    private static volatile List<Hit> results = new ArrayList<>();
    private static volatile int total = 0;
    private static volatile boolean loading = false;
    private static volatile boolean everSearched = false;
    private static volatile String status = "";
    private static final Map<String, String> INSTALL_STATE = new ConcurrentHashMap<>();

    private ModrinthBrowser() {
    }

    // ------------------------------------------------------------------ state for the GUI

    public static List<Hit> results() {
        return results;
    }

    public static int total() {
        return total;
    }

    public static void setStatus(String s) {
        status = s == null ? "" : s;
    }

    public static String statusLine() {
        if (loading) return "Working... please wait";
        if (!status.isEmpty()) return status;
        if (results.isEmpty()) return "";
        return results.size() + " of " + total + " mods shown";
    }

    public static String buttonLabel(Hit h) {
        String s = INSTALL_STATE.get(h.id);
        if (s != null) return s;
        return ModsManager.hasId(h.slug) ? "Installed" : "Install";
    }

    /** Runs the first search (most downloaded mods) the first time the Mods tab is opened. */
    public static void ensureLoaded() {
        if (!everSearched && !loading) search(false);
    }

    // ------------------------------------------------------------------ search

    public static String gameVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft")
                .map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("1.21.1");
    }

    public static void search(boolean append) {
        if (loading) return;
        loading = true;
        everSearched = true;
        status = "";
        ModsManager.dirty = true;
        final String q = query.trim();
        final Sort s = sort;
        final int offset = append ? results.size() : 0;
        Thread t = new Thread(() -> {
            try {
                String facets = "[[\"project_type:mod\"],[\"categories:fabric\"],[\"versions:" + gameVersion() + "\"],"
                        + "[\"client_side:required\",\"client_side:optional\"]]";
                String url = API + "/search?query=" + enc(q) + "&facets=" + enc(facets)
                        + "&index=" + s.api + "&limit=20&offset=" + offset;
                JsonObject o = JsonParser.parseString(getText(url)).getAsJsonObject();
                List<Hit> list = append ? new ArrayList<>(results) : new ArrayList<>();
                for (JsonElement el : o.getAsJsonArray("hits")) {
                    JsonObject h = el.getAsJsonObject();
                    list.add(new Hit(
                            str(h, "project_id"), str(h, "slug"), str(h, "title"),
                            str(h, "description"), str(h, "author"),
                            h.has("downloads") ? h.get("downloads").getAsLong() : 0L));
                }
                total = o.has("total_hits") ? o.get("total_hits").getAsInt() : list.size();
                results = list;
                status = list.isEmpty() ? "No mods found." : "";
            } catch (Throwable e) {
                status = "Search failed: " + shortMsg(e) + " (check your internet)";
            } finally {
                loading = false;
                ModsManager.dirty = true;
            }
        }, "Supersonic-Modrinth-Search");
        t.setDaemon(true);
        t.start();
    }

    // ------------------------------------------------------------------ install

    public static void install(Hit hit) {
        String cur = INSTALL_STATE.get(hit.id);
        if (cur != null && !cur.equals("Failed")) return; // already busy or done
        INSTALL_STATE.put(hit.id, "Installing...");
        status = "";
        ModsManager.dirty = true;
        Thread t = new Thread(() -> {
            try {
                installProject(hit.id, 0, new HashSet<>());
                INSTALL_STATE.put(hit.id, "Installed");
                status = hit.title + " installed. Restart the game to load it.";
            } catch (AlreadyInstalled e) {
                INSTALL_STATE.put(hit.id, "Installed");
                status = hit.title + " is already installed.";
            } catch (Throwable e) {
                INSTALL_STATE.put(hit.id, "Failed");
                status = "Install failed: " + shortMsg(e);
            } finally {
                ModsManager.dirty = true;
            }
        }, "Supersonic-Modrinth-Install");
        t.setDaemon(true);
        t.start();
    }

    private static void installProject(String projectId, int depth, Set<String> visited) throws IOException {
        if (depth > 3 || !visited.add(projectId)) return;
        String gv = gameVersion();
        String url = API + "/project/" + enc(projectId) + "/version?loaders=" + enc("[\"fabric\"]")
                + "&game_versions=" + enc("[\"" + gv + "\"]");
        JsonArray versions = JsonParser.parseString(getText(url)).getAsJsonArray();
        if (versions.size() == 0) throw new IOException("no Fabric build for Minecraft " + gv);

        JsonObject version = versions.get(0).getAsJsonObject(); // newest first
        for (JsonElement el : versions) {
            JsonObject v = el.getAsJsonObject();
            if ("release".equals(str(v, "version_type"))) {
                version = v;
                break;
            }
        }

        // required dependencies first
        if (version.has("dependencies") && version.get("dependencies").isJsonArray()) {
            for (JsonElement el : version.getAsJsonArray("dependencies")) {
                JsonObject d = el.getAsJsonObject();
                if (!"required".equals(str(d, "dependency_type"))) continue;
                String depId = str(d, "project_id");
                if (depId.isEmpty() || dependencyPresent(depId)) continue;
                try {
                    installProject(depId, depth + 1, visited);
                } catch (AlreadyInstalled ignored) {
                    // fine, it is there already
                }
            }
        }

        JsonArray files = version.getAsJsonArray("files");
        if (files == null || files.size() == 0) throw new IOException("the mod has no download file");
        JsonObject file = files.get(0).getAsJsonObject();
        for (JsonElement el : files) {
            JsonObject f = el.getAsJsonObject();
            if (f.has("primary") && f.get("primary").getAsBoolean()) {
                file = f;
                break;
            }
        }
        String sha1 = file.has("hashes") && file.getAsJsonObject("hashes").has("sha1")
                ? file.getAsJsonObject("hashes").get("sha1").getAsString() : null;
        download(str(file, "url"), str(file, "filename"), sha1);
    }

    private static boolean dependencyPresent(String depProjectId) {
        try {
            JsonObject p = JsonParser.parseString(getText(API + "/project/" + enc(depProjectId))).getAsJsonObject();
            ModsManager.scan();
            return ModsManager.hasId(str(p, "slug"));
        } catch (Throwable e) {
            return false;
        }
    }

    private static void download(String url, String fileName, String sha1) throws IOException {
        URI uri = URI.create(url);
        if (!"https".equals(uri.getScheme()) || !"cdn.modrinth.com".equals(uri.getHost())) {
            throw new IOException("unexpected download address");
        }
        String safe = new File(fileName).getName();
        if (!safe.endsWith(".jar")) throw new IOException("the download is not a .jar file");

        File dir = ModsManager.modsDir();
        File target = new File(dir, safe);
        if (target.exists() || new File(dir, safe + ".disabled").exists()) throw new AlreadyInstalled(safe);
        File part = new File(dir, safe + ".part");

        HttpURLConnection c = open(url);
        if (c.getResponseCode() != 200) throw new IOException("download failed (HTTP " + c.getResponseCode() + ")");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            try (InputStream in = new DigestInputStream(c.getInputStream(), md)) {
                Files.copy(in, part.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : md.digest()) hex.append(String.format("%02x", b));
            if (sha1 != null && !sha1.equalsIgnoreCase(hex.toString())) {
                throw new IOException("the downloaded file is damaged (checksum)");
            }
            // never put a second copy of a mod that is already installed under another file name
            ModsManager.scan();
            String newId = ModsManager.readId(part);
            if (!newId.isEmpty() && ModsManager.hasId(newId)) throw new AlreadyInstalled(newId);

            Files.move(part.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            ModsManager.markInstalled(safe);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("SHA-1 is not available");
        } finally {
            part.delete(); // does nothing when the file was moved
        }
    }

    // ------------------------------------------------------------------ http helpers

    private static HttpURLConnection open(String url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) URI.create(url).toURL().openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(30000);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", UA);
        c.setRequestProperty("Accept", "application/json");
        return c;
    }

    private static String getText(String url) throws IOException {
        HttpURLConnection c = open(url);
        int code = c.getResponseCode();
        if (code != 200) throw new IOException("HTTP " + code);
        try (InputStream in = c.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String str(JsonObject o, String key) {
        try {
            if (o.has(key) && !o.get(key).isJsonNull()) return o.get(key).getAsString();
        } catch (Throwable ignored) {
            // wrong type: treat as empty
        }
        return "";
    }

    private static String shortMsg(Throwable e) {
        String m = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return m.length() > 70 ? m.substring(0, 70) : m;
    }
}
