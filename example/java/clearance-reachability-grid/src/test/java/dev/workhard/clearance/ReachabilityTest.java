package dev.workhard.clearance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReachabilityTest {

    // 왼쪽 방과 오른쪽 방 사이의 문은 1칸 폭(둘째 줄)뿐이다
    private final String[] map = {
        "...#...",
        ".......",
        "...#...",
        "...#..."
    };

    @Test
    void sizeOneWalksThroughNarrowDoor() {
        assertTrue(Reachability.canReach(map, 0, 0, 0, 6, 1));
    }

    @Test
    void sizeTwoIsBlockedByNarrowDoor() {
        // 같은 길인데 2x2 영역이 문 칸에 들어가지 못해 막힌다
        assertFalse(Reachability.canReach(map, 0, 0, 0, 5, 2));
    }

    @Test
    void sizeTwoStillMovesInsideOpenRoom() {
        // 막히는 건 문뿐이고 방 안에서는 큰 이동체도 움직인다
        assertTrue(Reachability.canReach(map, 0, 0, 2, 1, 2));
    }

    @Test
    void startThatDoesNotFitIsUnreachable() {
        assertFalse(Reachability.canReach(map, 0, 2, 0, 0, 2));
    }
}
