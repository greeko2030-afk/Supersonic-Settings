package net.supersonic.hud;

import java.util.Arrays;

/** Rolling frame-time statistics measured from real rendered frames. */
public final class FrameStats {
    private static final int N = 300;
    private static final long[] SAMPLES = new long[N];
    private static int index = 0;
    private static int count = 0;
    private static long last = 0L;

    private FrameStats() {
    }

    public static void frame() {
        long now = System.nanoTime();
        if (last != 0L) {
            long d = now - last;
            if (d > 0 && d < 1_000_000_000L) {
                SAMPLES[index] = d;
                index = (index + 1) % N;
                if (count < N) count++;
            }
        }
        last = now;
    }

    public static double frameMs() {
        if (count == 0) return 0.0;
        long sum = 0L;
        for (int i = 0; i < count; i++) sum += SAMPLES[i];
        return sum / (double) count / 1_000_000.0;
    }

    public static double avgFps() {
        double ms = frameMs();
        return ms <= 0.0 ? 0.0 : 1000.0 / ms;
    }

    /** FPS of the slowest 1% of frames. */
    public static double lowFps() {
        if (count == 0) return 0.0;
        long[] copy = Arrays.copyOf(SAMPLES, count);
        Arrays.sort(copy);
        int i = Math.min(count - 1, (int) Math.floor(count * 0.99));
        return 1_000_000_000.0 / Math.max(1L, copy[i]);
    }
}
