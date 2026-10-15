package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BackoffProbeTest {

    private final Instant t0 = Instant.parse("2026-01-01T00:00:00Z");
    private final BackoffProbe probe = new BackoffProbe(Duration.ofSeconds(5), Duration.ofSeconds(60));

    @Test
    void intervalDoublesOnEachFailure() {
        assertEquals(Duration.ofSeconds(5), probe.currentInterval());
        probe.record(false, t0);
        assertEquals(Duration.ofSeconds(10), probe.currentInterval());
        probe.record(false, t0);
        assertEquals(Duration.ofSeconds(20), probe.currentInterval());
    }

    @Test
    void intervalIsCappedEvenWithManyFailures() {
        for (int i = 0; i < 100; i++) {
            probe.record(false, t0);
        }
        assertEquals(Duration.ofSeconds(60), probe.currentInterval());
    }

    @Test
    void successResetsToBaseInterval() {
        probe.record(false, t0);
        probe.record(false, t0);
        probe.record(true, t0);
        assertEquals(Duration.ofSeconds(5), probe.currentInterval());
    }

    @Test
    void notDueUntilNextScheduledTime() {
        probe.record(false, t0); // 다음 확인은 10초 뒤
        assertFalse(probe.isDue(t0.plusSeconds(9)));
        assertTrue(probe.isDue(t0.plusSeconds(10)));
    }
}
