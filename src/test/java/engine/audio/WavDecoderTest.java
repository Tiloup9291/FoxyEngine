package engine.audio;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;

/** WAV decoder (8/16-bit mono/stereo) + rejections. */
class WavDecoderTest {

    /** Build a minimal PCM WAV in memory. */
    private static byte[] wavBytes(int rate, int channels, int bits, int frames) throws Exception {
        int bytesPerSample = bits / 8;
        int dataLen = frames * channels * bytesPerSample;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        out.write(new byte[]{'R', 'I', 'F', 'F'});
        writeLE(out, 36 + dataLen, 4);
        out.write(new byte[]{'W', 'A', 'V', 'E', 'f', 'm', 't', ' '});
        writeLE(out, 16, 4);
        writeLE(out, 1, 2);
        writeLE(out, channels, 2);
        writeLE(out, rate, 4);
        writeLE(out, rate * channels * bytesPerSample, 4);
        writeLE(out, channels * bytesPerSample, 2);
        writeLE(out, bits, 2);
        out.write(new byte[]{'d', 'a', 't', 'a'});
        writeLE(out, dataLen, 4);
        for (int i = 0; i < frames * channels; i++) {
            if (bits == 16) { out.write(0); out.write(0x40); }
            else out.write(128);
        }
        return out.toByteArray();
    }

    private static void writeLE(java.io.OutputStream out, int v, int n) throws Exception {
        for (int i = 0; i < n; i++) out.write((v >> (8 * i)) & 0xFF);
    }

    @Test
    void decodeMono16() throws Exception {
        byte[] wav = wavBytes(22050, 1, 16, 100);
        AudioClip c = WavDecoder.decode(new ByteArrayInputStream(wav));
        assertEquals(22050, c.sampleRate());
        assertEquals(1, c.channels());
        assertEquals(100, c.frameCount());
    }

    @Test
    void decodeStereo8() throws Exception {
        byte[] wav = wavBytes(44100, 2, 8, 50);
        AudioClip c = WavDecoder.decode(new ByteArrayInputStream(wav));
        assertEquals(2, c.channels());
        assertEquals(50, c.frameCount());
    }

    @Test
    void junkRejected() {
        byte[] junk = {1, 2, 3, 4, 5};
        assertThrows(java.io.IOException.class,
                () -> WavDecoder.decode(new ByteArrayInputStream(junk)));
    }
}
