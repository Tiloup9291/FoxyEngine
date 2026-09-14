package engine.core;

import engine.core.FrameLimiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FrameLimiterTest {
    FrameLimiterTest() {
    }

    @Test
    void unlimitedHasZeroBudget() {
        Assertions.assertEquals((long)0L, (long)new FrameLimiter(0).frameBudgetNs());
    }

    @Test
    void budgetMatchesTarget() {
        Assertions.assertEquals((long)16666666L, (long)new FrameLimiter(60).frameBudgetNs());
        Assertions.assertEquals((long)8333333L, (long)new FrameLimiter(120).frameBudgetNs());
    }

    @Test
    void cappedFrameTakesAtLeastBudget() {
        FrameLimiter limiter = new FrameLimiter(200);
        limiter.beginFrame();
        long elapsed = limiter.endFrame();
        Assertions.assertTrue((elapsed >= 4000000L ? 1 : 0) != 0, (String)("frame limitee a ~5ms, mesure=" + elapsed));
    }

    @Test
    void unlimitedReturnsImmediately() {
        FrameLimiter limiter = new FrameLimiter(0);
        limiter.beginFrame();
        long elapsed = limiter.endFrame();
        Assertions.assertTrue((elapsed < 500000000L ? 1 : 0) != 0);
    }
}
