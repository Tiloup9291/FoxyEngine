package engine.audio;

import engine.audio.AudioClip;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class WavDecoder {
    private WavDecoder() {
    }

    public static AudioClip decode(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file, new OpenOption[0]);){
            AudioClip audioClip = WavDecoder.decode(in);
            return audioClip;
        }
    }

    public static AudioClip decode(File file) throws IOException {
        return WavDecoder.decode(file.toPath());
    }

    public static AudioClip decode(InputStream in) throws IOException {
        AudioInputStream src;
        try {
            src = AudioSystem.getAudioInputStream(in);
        }
        catch (UnsupportedAudioFileException e) {
            throw new IOException("unsupported audio format (WAV PCM expected)", e);
        }
        try (AudioInputStream stream = src;){
            AudioClip audioClip;
            block18: {
                AudioFormat base = stream.getFormat();
                int channels = base.getChannels();
                if (channels != 1 && channels != 2) {
                    throw new IOException("unsupported channels: " + channels);
                }
                AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, base.getSampleRate(), 16, channels, channels * 2, base.getSampleRate(), false);
                AudioInputStream pcm = AudioSystem.getAudioInputStream(target, stream);
                try {
                    byte[] raw = pcm.readAllBytes();
                    int frames = raw.length / (channels * 2);
                    if (frames == 0) {
                        throw new IOException("empty WAV file");
                    }
                    float[] samples = new float[frames * channels];
                    for (int f = 0; f < frames; ++f) {
                        for (int c = 0; c < channels; ++c) {
                            int i = (f * channels + c) * 2;
                            int lo = raw[i] & 0xFF;
                            byte hi = raw[i + 1];
                            samples[f * channels + c] = (float)(hi << 8 | lo) / 32768.0f;
                        }
                    }
                    audioClip = new AudioClip((int)target.getSampleRate(), channels, samples);
                    if (pcm == null) break block18;
                }
                catch (Throwable throwable) {
                    if (pcm != null) {
                        try {
                            pcm.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                pcm.close();
            }
            return audioClip;
        }
    }
}
