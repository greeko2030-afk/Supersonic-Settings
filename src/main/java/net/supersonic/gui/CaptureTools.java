package net.supersonic.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.supersonic.SupersonicClient;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Real helpers for the Replay & Capture tab: folders, file lists, screenshots, clipboard. */
public final class CaptureTools {
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private static int pendingShot = 0;
    private static boolean hudBefore = false;
    public static volatile String lastCopied = "";

    private CaptureTools() {
    }

    public static File dir(String name) {
        return FabricLoader.getInstance().getGameDir().resolve(name).toFile();
    }

    public static List<File> files(String dirName, String ext) {
        File[] fs = dir(dirName).listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(ext));
        List<File> list = new ArrayList<>();
        if (fs != null) list.addAll(Arrays.asList(fs));
        list.sort(Comparator.comparingLong(File::lastModified).reversed());
        return list;
    }

    public static List<File> recent(String dirName, String ext, int max) {
        List<File> all = files(dirName, ext);
        return all.size() > max ? all.subList(0, max) : all;
    }

    public static long totalSize(String dirName, String ext) {
        long sum = 0L;
        for (File f : files(dirName, ext)) sum += f.length();
        return sum;
    }

    public static int deleteOlderThan(String dirName, String ext, int days) {
        long cutoff = System.currentTimeMillis() - days * 86_400_000L;
        int n = 0;
        for (File f : files(dirName, ext)) {
            if (f.lastModified() < cutoff && f.delete()) n++;
        }
        return n;
    }

    public static String size(long bytes) {
        if (bytes < 1024L) return bytes + " B";
        if (bytes < 1024L * 1024L) return String.format(Locale.ROOT, "%.0f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024L * 1024L) return String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
        return String.format(Locale.ROOT, "%.2f GB", bytes / 1073741824.0);
    }

    public static String date(long ms) {
        return DATE.format(Instant.ofEpochMilli(ms));
    }

    // ----------------------------------------------------------- screenshot

    /** Closes the menu and takes a screenshot a few ticks later (so the menu is not in the picture). */
    public static void requestScreenshot(MinecraftClient mc) {
        hudBefore = mc.options.hudHidden;
        if (SupersonicClient.CONFIG.screenshotHideHud) mc.options.hudHidden = true;
        pendingShot = 4;
        mc.setScreen(null);
    }

    public static void tick(MinecraftClient mc) {
        if (pendingShot <= 0) return;
        pendingShot--;
        if (pendingShot == 0) {
            ScreenshotRecorder.saveScreenshot(mc.runDirectory, mc.getFramebuffer(),
                    msg -> mc.execute(() -> mc.inGameHud.getChatHud().addMessage(msg)));
            mc.options.hudHidden = hudBefore;
        }
    }

    // ------------------------------------------------------------ clipboard

    public static void copy(MinecraftClient mc, String text) {
        mc.keyboard.setClipboard(text);
        lastCopied = text;
    }
}
