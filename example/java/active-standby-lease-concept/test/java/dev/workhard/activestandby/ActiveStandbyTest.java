package dev.workhard.activestandby;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class ActiveStandbyTest {
    private final AtomicLong now = new AtomicLong(0);
    private final LeaseStore store = new LeaseStore(now::get, 10);
    private final Node a = new Node("A", store);
    private final Node b = new Node("B", store);
    private final Router router = new Router(store, Map.of("A", a, "B", b));

    @Test
    void onlyFirstNodeBecomesActive() {
        a.tick();
        b.tick();
        assertTrue(a.isActive());
        assertFalse(b.isActive());
        assertEquals("A:req", router.send("req"));
        assertEquals(1, store.term());
    }

    @Test
    void standbyTakesOverAfterLeaseExpires() {
        a.tick();                  // t=0, 만료 10
        now.set(5);
        b.tick();                  // 아직 A가 유효
        assertFalse(b.isActive());
        now.set(10);               // A는 갱신을 멈춤 -> 만료
        b.tick();
        assertTrue(b.isActive());
        assertEquals("B:req", router.send("req"));
        assertEquals(2, store.term());
    }

    @Test
    void activeKeepsLeaseByRenewing() {
        a.tick();
        now.set(8);
        a.tick();                  // 갱신 -> 만료 18
        now.set(9);
        b.tick();
        assertFalse(b.isActive());
        now.set(16);
        a.tick();                  // 갱신 -> 만료 26
        now.set(17);
        b.tick();
        assertFalse(b.isActive());
        assertEquals(1, store.term()); // 같은 노드의 갱신은 term을 올리지 않는다
    }

    @Test
    void standbyRejectsDirectRequest() {
        a.tick();
        b.tick();
        assertThrows(IllegalStateException.class, () -> b.handle("req"));
    }
}
