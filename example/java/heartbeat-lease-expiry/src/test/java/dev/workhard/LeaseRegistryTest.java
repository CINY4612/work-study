package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LeaseRegistryTest {

    /** 테스트에서 시간을 마음대로 흘려보내는 Clock. */
    static class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private final MutableClock clock = new MutableClock();
    private final LeaseRegistry registry = new LeaseRegistry(clock, Duration.ofSeconds(15));

    @Test
    void aliveUntilTtlPassesWithoutHeartbeat() {
        registry.heartbeat("worker-1");
        clock.advance(Duration.ofSeconds(14));
        assertTrue(registry.isAlive("worker-1"));
        clock.advance(Duration.ofSeconds(1)); // 정확히 만료 시각
        assertFalse(registry.isAlive("worker-1"));
    }

    @Test
    void heartbeatExtendsLease() {
        registry.heartbeat("worker-1");
        clock.advance(Duration.ofSeconds(10));
        registry.heartbeat("worker-1");
        clock.advance(Duration.ofSeconds(10));
        assertTrue(registry.isAlive("worker-1"));
    }

    @Test
    void acquireFailsWhileValidAndSucceedsAfterExpiry() {
        assertTrue(registry.tryAcquire("slot-1"));
        assertFalse(registry.tryAcquire("slot-1"));
        clock.advance(Duration.ofSeconds(15));
        assertTrue(registry.tryAcquire("slot-1"));
    }
}
