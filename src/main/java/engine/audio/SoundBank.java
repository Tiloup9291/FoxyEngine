package engine.audio;

import engine.audio.AudioClip;
import engine.audio.SynthSounds;
import engine.audio.WavDecoder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class SoundBank {
    private final Path soundsDir;
    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap();

    public SoundBank(Path soundsDir) {
        this.soundsDir = soundsDir;
    }

    public AudioClip acquire(String id) throws IOException {
        Entry existing = this.cache.get(id);
        if (existing != null) {
            existing.refs.incrementAndGet();
            return existing.clip;
        }
        AudioClip loaded = this.load(id);
        Entry fresh = new Entry(loaded);
        Entry prev = this.cache.putIfAbsent(id, fresh);
        Entry winner = prev != null ? prev : fresh;
        winner.refs.incrementAndGet();
        return winner.clip;
    }

    public void release(String id) {
        Entry e = this.cache.get(id);
        if (e == null) {
            return;
        }
        if (e.refs.decrementAndGet() <= 0) {
            this.cache.remove(id, e);
        }
    }

    public int refCount(String id) {
        Entry e = this.cache.get(id);
        return e == null ? 0 : e.refs.get();
    }

    public boolean isCached(String id) {
        return this.cache.containsKey(id);
    }

    public int cachedCount() {
        return this.cache.size();
    }

    public Map<String, Integer> snapshotRefs() {
        HashMap<String, Integer> m = new HashMap<String, Integer>();
        this.cache.forEach((k, v) -> m.put((String)k, v.refs.get()));
        return m;
    }

    public List<String> availableSounds() throws IOException {
        ArrayList<String> out = new ArrayList<String>();
        if (!Files.isDirectory(this.soundsDir, new LinkOption[0])) {
            return out;
        }
        try (Stream<Path> stream = Files.list(this.soundsDir);){
            for (Path p : (Iterable<Path>)stream::iterator) {
                String n = p.getFileName().toString().toLowerCase();
                if (!n.endsWith(".wav")) continue;
                out.add(n.substring(0, n.length() - 4));
            }
        }
        out.sort(String::compareTo);
        return out;
    }

    private AudioClip load(String id) throws IOException {
        AudioClip synth = SynthSounds.get(id);
        if (synth != null) {
            return synth;
        }
        Path wav = this.soundsDir.resolve(id + ".wav").normalize();
        if (Files.exists(wav, new LinkOption[0])) {
            return WavDecoder.decode(wav);
        }
        Path ogg = this.soundsDir.resolve(id + ".ogg").normalize();
        if (Files.exists(ogg, new LinkOption[0])) {
            throw new IOException("OGG unsupported in pure JDK (javax.sound does not decode OGG): " + ogg + " \u2014 convert to 16-bit PCM WAV");
        }
        throw new IOException("sound not found: " + wav + " (nor synthetic '" + id + "')");
    }

    private static final class Entry {
        final AudioClip clip;
        final AtomicInteger refs = new AtomicInteger(0);

        Entry(AudioClip clip) {
            this.clip = clip;
        }
    }
}
