package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WorkRouterTest {

    private WorkRouter newRouter() {
        WorkRouter r = new WorkRouter("lead");
        r.register("dev");
        return r;
    }

    @Test
    void workerWithPendingWorkLeavesAfterFinishing() {
        WorkRouter r = newRouter();
        r.assign("dev");
        r.sendOnLeave("dev");
        assertEquals(WorkRouter.Status.LEAVING, r.status("dev"));
        assertEquals("lead", r.assign("dev")); // 새 요청은 조정자에게
        r.finish("dev");
        assertEquals(WorkRouter.Status.AWAY, r.status("dev"));
    }

    @Test
    void coordinatorCannotLeave() {
        WorkRouter r = newRouter();
        assertThrows(IllegalArgumentException.class, () -> r.sendOnLeave("lead"));
    }

    @Test
    void alertsOnlyOnceAfterLimit() {
        WorkRouter r = newRouter();
        r.sendOnLeave("dev");
        for (int i = 0; i < 6; i++) {
            r.assign("dev");
        }
        assertEquals(1, r.alerts().size());
    }

    @Test
    void comingBackResetsBounceCount() {
        WorkRouter r = newRouter();
        r.sendOnLeave("dev");
        for (int i = 0; i < 4; i++) {
            r.assign("dev");
        }
        r.comeBack("dev");
        r.sendOnLeave("dev");
        for (int i = 0; i < 4; i++) {
            r.assign("dev");
        }
        assertEquals(2, r.alerts().size());
    }
}
