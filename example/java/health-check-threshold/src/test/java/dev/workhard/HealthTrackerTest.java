package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HealthTrackerTest {

    @Test
    void downOnlyAfterConsecutiveFailures() {
        HealthTracker tracker = new HealthTracker(3);
        assertTrue(tracker.report("a", false).isEmpty());
        assertTrue(tracker.report("a", false).isEmpty());
        var change = tracker.report("a", false); // 3번째 연속 실패
        assertEquals(HealthTracker.Status.DOWN, change.orElseThrow().to());
    }

    @Test
    void noRepeatedChangeWhileStillDown() {
        HealthTracker tracker = new HealthTracker(1);
        assertTrue(tracker.report("a", false).isPresent());
        assertTrue(tracker.report("a", false).isEmpty()); // 이미 DOWN이면 알림 없음
    }

    @Test
    void successResetsFailureCount() {
        HealthTracker tracker = new HealthTracker(2);
        tracker.report("a", false);
        tracker.report("a", true); // 횟수 초기화
        assertTrue(tracker.report("a", false).isEmpty()); // 다시 1회째라 DOWN 아님
    }

    @Test
    void recoveryReportsChangeToUp() {
        HealthTracker tracker = new HealthTracker(1);
        tracker.report("a", false);
        var change = tracker.report("a", true);
        assertEquals(HealthTracker.Status.UP, change.orElseThrow().to());
    }
}
