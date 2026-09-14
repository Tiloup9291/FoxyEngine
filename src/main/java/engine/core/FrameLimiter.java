package engine.core;

public final class FrameLimiter {
    private long frameBudgetNs;
    private long frameStartNs;

    public FrameLimiter(int fpsTarget) {
        this.frameBudgetNs = FrameLimiter.budgetFor(fpsTarget);
    }

    public void setTarget(int fpsTarget) {
        this.frameBudgetNs = FrameLimiter.budgetFor(fpsTarget);
    }

    static long budgetFor(int fpsTarget) {
        if (fpsTarget <= 0 || fpsTarget > 120) {
            if (fpsTarget <= 0) {
                return 0L;
            }
            fpsTarget = 120;
        }
        return 1000000000L / (long) fpsTarget;
    }

    public void beginFrame() {
        this.frameStartNs = System.nanoTime();
    }

    public long endFrame() {
        long elapsed = System.nanoTime() - this.frameStartNs;
        if (this.frameBudgetNs > 0L) {
            long remaining = this.frameBudgetNs - elapsed;
            if (remaining > 2000000L) {
                try {
                    Thread.sleep((remaining - 1000000L) / 1000000L);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return System.nanoTime() - this.frameStartNs;
                }
            }
            while (System.nanoTime() - this.frameStartNs < this.frameBudgetNs) {
                Thread.onSpinWait();
            }
        }
        return System.nanoTime() - this.frameStartNs;
    }

    public long frameBudgetNs() {
        return this.frameBudgetNs;
    }
}
