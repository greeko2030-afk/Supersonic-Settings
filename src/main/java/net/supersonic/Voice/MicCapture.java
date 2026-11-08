package net.supersonic.voice;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Microphone input, 16 kHz / 16-bit / mono / little endian.
 * Tries OpenAL first (the audio library Minecraft already ships, also used on Android launchers),
 * then falls back to Java Sound.
 *
 * The OpenAL capture device is opened once and kept for the whole game session: closing and re-opening it
 * is unreliable on Android. Turning the microphone off only stops recording.
 */
public final class MicCapture implements AutoCloseable {
    private static final AudioFormat FORMAT = new AudioFormat(16000f, 16, 1, true, false);
    public static volatile String lastSource = "-";

    private static long sharedDevice = 0L;
    private static boolean hookAdded = false;

    private interface Source {
        int read(byte[] buf) throws Exception;

        void close();

        String name();
    }

    private final Source source;

    private MicCapture(Source source) {
        this.source = source;
        lastSource = source.name();
    }

    public static MicCapture open() throws Exception {
        Throwable openAlError = null;
        try {
            return new MicCapture(new OpenAlSource());
        } catch (Throwable t) {
            openAlError = t;
        }
        try {
            return new MicCapture(new JavaSoundSource());
        } catch (Throwable t2) {
            String a = openAlError.getMessage() == null ? openAlError.getClass().getSimpleName() : openAlError.getMessage();
            String b = t2.getMessage() == null ? t2.getClass().getSimpleName() : t2.getMessage();
            throw new Exception("No microphone available (OpenAL: " + a + "; Java Sound: " + b + ")");
        }
    }

    public String sourceName() {
        return source.name();
    }

    /** Reads up to buf.length bytes; returns 0 when nothing is ready yet. */
    public int read(byte[] buf) throws Exception {
        return source.read(buf);
    }

    @Override
    public void close() {
        source.close();
    }

    // ------------------------------------------------------------- OpenAL

    /** Returns the shared capture device, opening it if needed (or re-opening it when forced). */
    private static synchronized long device(boolean forceReopen) {
        if (forceReopen && sharedDevice != 0L) {
            try {
                ALC11.alcCaptureStop(sharedDevice);
                ALC11.alcCaptureCloseDevice(sharedDevice);
            } catch (Throwable ignored) {
                // the old handle may already be invalid
            }
            sharedDevice = 0L;
        }
        if (sharedDevice == 0L) {
            sharedDevice = ALC11.alcCaptureOpenDevice((ByteBuffer) null, 16000, AL10.AL_FORMAT_MONO16, 16000);
            if (sharedDevice != 0L && !hookAdded) {
                hookAdded = true;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    long d = sharedDevice;
                    if (d != 0L) {
                        try {
                            ALC11.alcCaptureStop(d);
                            ALC11.alcCaptureCloseDevice(d);
                        } catch (Throwable ignored) {
                            // shutting down anyway
                        }
                    }
                }));
            }
        }
        return sharedDevice;
    }

    private static final class OpenAlSource implements Source {
        private final long device;
        private long lastData;
        private boolean restarted = false;

        OpenAlSource() throws Exception {
            long d = 0L;
            for (int attempt = 0; attempt < 8 && d == 0L; attempt++) {
                d = device(attempt >= 2); // after two failures throw the old handle away and open a fresh one
                if (d == 0L) Thread.sleep(250L);
            }
            if (d == 0L) throw new IllegalStateException("capture device unavailable");
            device = d;
            ALC11.alcCaptureStart(device);

            // throw away anything recorded before this session
            int pending = ALC10.alcGetInteger(device, ALC11.ALC_CAPTURE_SAMPLES);
            if (pending > 0) {
                ByteBuffer junk = BufferUtils.createByteBuffer(pending * 2);
                ALC11.alcCaptureSamples(device, junk, pending);
            }
            lastData = System.currentTimeMillis();
        }

        @Override
        public int read(byte[] out) throws Exception {
            int available = ALC10.alcGetInteger(device, ALC11.ALC_CAPTURE_SAMPLES);
            long now = System.currentTimeMillis();
            if (available <= 0) {
                if (now - lastData > 4000L) {
                    if (!restarted) {
                        restarted = true;
                        ALC11.alcCaptureStop(device);
                        ALC11.alcCaptureStart(device);
                        lastData = now;
                    } else {
                        throw new IllegalStateException("no audio from the microphone "
                                + "(is microphone permission allowed for your launcher?)");
                    }
                }
                Thread.sleep(10L);
                return 0;
            }
            lastData = now;
            int n = Math.min(available, out.length / 2);
            ByteBuffer bb = BufferUtils.createByteBuffer(n * 2).order(ByteOrder.LITTLE_ENDIAN);
            ALC11.alcCaptureSamples(device, bb, n);
            bb.get(out, 0, n * 2);
            return n * 2;
        }

        @Override
        public void close() {
            // Only stop recording. The device stays open for the next session.
            try {
                ALC11.alcCaptureStop(device);
            } catch (Throwable ignored) {
                // already stopped
            }
        }

        @Override
        public String name() {
            return "OpenAL";
        }
    }

    // ---------------------------------------------------------- Java Sound

    private static final class JavaSoundSource implements Source {
        private final TargetDataLine line;

        JavaSoundSource() throws Exception {
            line = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, FORMAT));
            line.open(FORMAT);
            line.start();
        }

        @Override
        public int read(byte[] out) {
            return line.read(out, 0, out.length);
        }

        @Override
        public void close() {
            try {
                line.stop();
                line.close();
            } catch (Throwable ignored) {
                // already closed
            }
        }

        @Override
        public String name() {
            return "Java Sound";
        }
    }
}
