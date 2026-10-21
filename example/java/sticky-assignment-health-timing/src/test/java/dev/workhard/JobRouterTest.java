package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class JobRouterTest {

    @Test
    void newAssignmentSkipsDownNode() {
        JobRouter r = new JobRouter(List.of("n1", "n2"));
        r.markDown("n1");
        assertEquals("n2", r.assign("job-1"));
    }

    @Test
    void assignedJobKeepsNodeEvenWhenItGoesDown() {
        JobRouter r = new JobRouter(List.of("n1", "n2"));
        assertEquals("n1", r.assign("job-1"));
        r.markDown("n1");
        assertEquals("n1", r.route("job-1"));   // 헬스 정보로 끊지 않는다
        assertEquals(1, r.assignedCount());     // 배정 정보도 지워지지 않는다
    }

    @Test
    void flappingNodeDoesNotLoseAssignment() {
        JobRouter r = new JobRouter(List.of("n1"));
        r.assign("job-1");
        r.markDown("n1");
        r.markUp("n1");
        assertEquals("n1", r.route("job-1"));
    }

    @Test
    void finishIsTheOnlyWayToRemoveAssignment() {
        JobRouter r = new JobRouter(List.of("n1"));
        r.assign("job-1");
        r.finish("job-1");
        assertEquals(0, r.assignedCount());
        assertThrows(NoSuchElementException.class, () -> r.route("job-1"));
    }

    @Test
    void noAliveNodeFailsNewAssignment() {
        JobRouter r = new JobRouter(List.of("n1"));
        r.markDown("n1");
        assertThrows(IllegalStateException.class, () -> r.assign("job-1"));
    }
}
