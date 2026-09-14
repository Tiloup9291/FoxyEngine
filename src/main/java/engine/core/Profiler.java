package engine.core;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Profiler {
    private final String[] names;
    private final long[] totalNs;
    private final long[] count;
    private final long[] lastNs;
    private final long[] startNs;
    private final boolean[] open;
    private long frameCount;

    public Profiler(String ... sections) {
        if (sections == null || sections.length == 0) {
            throw new IllegalArgumentException("empty sections");
        }
        this.names = (String[])sections.clone();
        this.totalNs = new long[this.names.length];
        this.count = new long[this.names.length];
        this.lastNs = new long[this.names.length];
        this.startNs = new long[this.names.length];
        this.open = new boolean[this.names.length];
    }

    public int indexOf(String name) {
        for (int i = 0; i < this.names.length; ++i) {
            if (!this.names[i].equals(name)) continue;
            return i;
        }
        return -1;
    }

    public void begin(int idx) {
        this.startNs[idx] = System.nanoTime();
        this.open[idx] = true;
    }

    public void end(int idx) {
        if (!this.open[idx]) {
            return;
        }
        long dt = System.nanoTime() - this.startNs[idx];
        int n = idx;
        this.totalNs[n] = this.totalNs[n] + dt;
        int n2 = idx;
        this.count[n2] = this.count[n2] + 1L;
        this.lastNs[idx] = dt;
        this.open[idx] = false;
    }

    public void nextFrame() {
        ++this.frameCount;
    }

    public long frameCount() {
        return this.frameCount;
    }

    public double avgMs(int idx) {
        return this.count[idx] == 0L ? 0.0 : (double)this.totalNs[idx] / 1000000.0 / (double)this.count[idx];
    }

    public float lastMs(int idx) {
        return (float)this.lastNs[idx] / 1000000.0f;
    }

    public void reset() {
        Arrays.fill(this.totalNs, 0L);
        Arrays.fill(this.count, 0L);
        Arrays.fill(this.lastNs, 0L);
        this.frameCount = 0L;
    }

    public String hudLine() {
        StringBuilder sb = new StringBuilder("prof");
        for (int i = 0; i < this.names.length; ++i) {
            sb.append(' ').append(this.names[i]).append('=');
            sb.append(String.format("%.1f", this.avgMs(i)));
        }
        sb.append("ms");
        return sb.toString();
    }

    public Map<String, Double> snapshotAvgMs() {
        LinkedHashMap<String, Double> m = new LinkedHashMap<String, Double>();
        for (int i = 0; i < this.names.length; ++i) {
            m.put(this.names[i], this.avgMs(i));
        }
        return m;
    }
}
