package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CleanupTriggerTest {

    @Test
    void 연속_3번째에_정리를_요청한다() {
        CleanupTrigger t = new CleanupTrigger(3);
        assertFalse(t.onCycle(true));
        assertFalse(t.onCycle(true));
        assertTrue(t.onCycle(true));
    }

    @Test
    void 장애가_이어져도_한_번만_요청한다() {
        CleanupTrigger t = new CleanupTrigger(2);
        t.onCycle(true);
        assertTrue(t.onCycle(true));
        assertFalse(t.onCycle(true));
    }

    @Test
    void 중간에_복구되면_연속_횟수를_다시_센다() {
        CleanupTrigger t = new CleanupTrigger(3);
        t.onCycle(true);
        t.onCycle(true);
        t.onCycle(false);
        assertFalse(t.onCycle(true));
        assertFalse(t.onCycle(true));
    }

    @Test
    void 복구_후_다시_죽으면_다시_정리한다() {
        CleanupTrigger t = new CleanupTrigger(2);
        t.onCycle(true);
        assertTrue(t.onCycle(true));
        t.onCycle(false);
        t.onCycle(true);
        assertTrue(t.onCycle(true));
    }

    @Test
    void 정리가_실패하면_다음_주기에_재시도한다() {
        CleanupTrigger t = new CleanupTrigger(1);
        assertTrue(t.onCycle(true));
        t.cleanupFailed();
        assertTrue(t.onCycle(true));
    }

    @Test
    void threshold가_0이면_동작하지_않는다() {
        CleanupTrigger t = new CleanupTrigger(0);
        assertFalse(t.onCycle(true));
    }
}
