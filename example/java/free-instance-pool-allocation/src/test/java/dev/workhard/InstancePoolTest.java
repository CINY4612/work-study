package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class InstancePoolTest {

    @Test
    void sameRoomGetsSameInstance() {
        InstancePool pool = new InstancePool();
        pool.register("a:1", 10);
        pool.register("a:2", 20);

        String first = pool.allocate("room1").orElseThrow();
        String again = pool.allocate("room1").orElseThrow();

        assertEquals(first, again);
        assertEquals("a:2", pool.allocate("room2").orElseThrow()); // 두 번째 룸은 남은 인스턴스
    }

    @Test
    void oldestFreeInstanceGoesFirst() {
        InstancePool pool = new InstancePool();
        pool.register("a:1", 30);
        pool.register("a:2", 10);

        assertEquals("a:2", pool.allocate("room1").orElseThrow());
    }

    @Test
    void deadInstanceIsSkipped() {
        InstancePool pool = new InstancePool();
        pool.register("a:1", 10);
        pool.register("a:2", 20);
        pool.markDead("a:1");

        assertEquals("a:2", pool.allocate("room1").orElseThrow());
    }

    @Test
    void releasedInstanceGoesToTheBackOfTheQueue() {
        InstancePool pool = new InstancePool();
        pool.register("a:1", 10);
        pool.register("a:2", 20);
        pool.allocate("room1"); // a:1 사용
        pool.release("room1", 100); // a:1은 시각 100으로 다시 비움

        assertEquals("a:2", pool.allocate("room2").orElseThrow());
    }

    @Test
    void noCandidateReturnsEmpty() {
        InstancePool pool = new InstancePool();
        pool.register("a:1", 10);
        pool.markDead("a:1");

        Optional<String> result = pool.allocate("room1");
        assertTrue(result.isEmpty());
    }
}
