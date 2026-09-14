package engine.audio;

import engine.audio.AudioClip;
import engine.audio.AudioMixer;
import engine.audio.Spatializer;
import engine.math.Vec3;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public final class AudioThread {
    public static final int OUTPUT_RATE = 44100;
    private static final int FRAMES_PER_TICK = 1024;
    private final ConcurrentLinkedQueue<Cmd> queue = new ConcurrentLinkedQueue();
    private final AudioMixer mixer = new AudioMixer();
    private volatile AudioClip musicClip;
    private volatile Vec3 listenerPos = Vec3.ZERO;
    private volatile float listenerYaw;
    private volatile float spatialMaxDist = 18.0f;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread thread;
    private SourceDataLine line;
    private volatile boolean lineAvailable;
    private volatile long mixedFrames;
    private volatile String lastError;

    public void setListener(Vec3 pos, float yaw) {
        this.listenerPos = pos;
        this.listenerYaw = yaw;
    }

    public void setSpatialMaxDist(float d) {
        this.spatialMaxDist = Math.max(1.0f, d);
    }

    public boolean lineAvailable() {
        return this.lineAvailable;
    }

    public long mixedFrames() {
        return this.mixedFrames;
    }

    public String lastError() {
        return this.lastError;
    }

    public int voiceCount() {
        return this.mixer.voiceCount();
    }

    public void play(AudioClip clip, float gain) {
        this.queue.add(new Cmd.Play(clip, gain, 0.0f, 1.0, false));
    }

    public void play(AudioClip clip, float gain, float pan, double pitch, boolean loop) {
        this.queue.add(new Cmd.Play(clip, gain, pan, pitch, loop));
    }

    public void playAt(AudioClip clip, Vec3 worldPos) {
        this.queue.add(new Cmd.PlayAt(clip, worldPos, this.spatialMaxDist));
    }

    public void stopLoop(AudioClip clip) {
        this.queue.add(new Cmd.StopLoop(clip));
    }

    public void setMaster(float v) {
        this.queue.add(new Cmd.Master(v));
    }

    public void playMusic(AudioClip clip, float gain) {
        this.queue.add(new Cmd.Music(clip, gain));
    }

    public void stopMusic() {
        this.queue.add(new Cmd.MusicStop());
    }

    public void mixAhead(float[] out, int frames) {
        this.drain();
        this.mixer.mix(out, frames, true);
        this.mixedFrames += (long)frames;
    }

    public void start() {
        if (this.running.getAndSet(true)) {
            return;
        }
        try {
            AudioFormat fmt = new AudioFormat(44100.0f, 16, 2, true, false);
            this.line = AudioSystem.getSourceDataLine(fmt);
            this.line.open(fmt, 11025);
            this.line.start();
            this.lineAvailable = true;
        }
        catch (Exception e) {
            this.lastError = "audio line unavailable: " + e.getMessage();
            this.line = null;
            this.lineAvailable = false;
        }
        this.thread = new Thread(this::loop, "AudioThread");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public void stop() {
        this.running.set(false);
        if (this.thread != null) {
            try {
                this.thread.join(1500L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.thread = null;
        }
        if (this.line != null) {
            try {
                this.line.drain();
                this.line.stop();
                this.line.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.line = null;
        }
        this.lineAvailable = false;
    }

    private void loop() {
        float[] mixBuf = new float[2048];
        byte[] pcm = new byte[4096];
        while (this.running.get()) {
            this.drain();
            this.mixer.mix(mixBuf, 1024, true);
            this.mixedFrames += 1024L;
            if (this.line != null) {
                for (int i = 0; i < 2048; ++i) {
                    int v = Math.round(mixBuf[i] * 32767.0f);
                    v = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, v));
                    pcm[i * 2] = (byte)(v & 0xFF);
                    pcm[i * 2 + 1] = (byte)(v >> 8 & 0xFF);
                }
                try {
                    this.line.write(pcm, 0, pcm.length);
                }
                catch (Exception e) {
                    this.lastError = "writing audio : " + e.getMessage();
                    AudioThread.sleepQuiet(20L);
                }
                continue;
            }
            AudioThread.sleepQuiet(20L);
        }
    }

    private static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void drain() {
        Cmd c;
        while ((c = this.queue.poll()) != null) {
            if (c instanceof Cmd.Play) {
                Cmd.Play p = (Cmd.Play)c;
                this.mixer.play(p.clip(), p.gain(), p.pan(), p.pitch(), p.loop());
                continue;
            }
            if (c instanceof Cmd.PlayAt) {
                float rz;
                Cmd.PlayAt p = (Cmd.PlayAt)c;
                float dx = p.pos().x() - this.listenerPos.x();
                float dz = p.pos().z() - this.listenerPos.z();
                float sy = (float)Math.sin(-this.listenerYaw);
                float cy = (float)Math.cos(-this.listenerYaw);
                float rx = dx * cy - dz * sy;
                Spatializer.SpatialGain sg = Spatializer.spatialize(rx, rz = dx * sy + dz * cy, p.maxDist());
                if (!(sg.gain() > 0.001f)) continue;
                this.mixer.play(p.clip(), sg.gain(), sg.pan(), 1.0, false);
                continue;
            }
            if (c instanceof Cmd.StopLoop) {
                Cmd.StopLoop s = (Cmd.StopLoop)c;
                this.mixer.stopLoop(s.clip());
                continue;
            }
            if (c instanceof Cmd.Master) {
                Cmd.Master m = (Cmd.Master)c;
                this.mixer.setMaster(m.v());
                continue;
            }
            if (c instanceof Cmd.Music) {
                Cmd.Music m = (Cmd.Music)c;
                if (this.musicClip != null) {
                    this.mixer.stopLoop(this.musicClip);
                }
                this.musicClip = m.clip();
                this.mixer.play(this.musicClip, m.gain(), 0.0f, 1.0, true);
                continue;
            }
            if (!(c instanceof Cmd.MusicStop) || this.musicClip == null) continue;
            this.mixer.stopLoop(this.musicClip);
            this.musicClip = null;
        }
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    private static interface Cmd {

        public record MusicStop() implements Cmd
        {
        }

        public record Music(AudioClip clip, float gain) implements Cmd
        {
        }

        public record Master(float v) implements Cmd
        {
        }

        public record StopLoop(AudioClip clip) implements Cmd
        {
        }

        public record PlayAt(AudioClip clip, Vec3 pos, float maxDist) implements Cmd
        {
        }

        public record Play(AudioClip clip, float gain, float pan, double pitch, boolean loop) implements Cmd
        {
        }
    }
}
