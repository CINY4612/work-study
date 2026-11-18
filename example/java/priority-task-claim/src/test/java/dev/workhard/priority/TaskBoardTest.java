package dev.workhard.priority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.workhard.priority.TaskBoard.Kind;
import dev.workhard.priority.TaskBoard.Task;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaskBoardTest {

    private final List<Kind> priority = List.of(Kind.CHECKOUT, Kind.CLEAN, Kind.RESTOCK);

    @Test
    void priorityIsReevaluatedAfterEachTask() {
        TaskBoard board = new TaskBoard();
        board.add(new Task("c1", Kind.CLEAN));
        board.add(new Task("c2", Kind.CLEAN));

        Task first = board.claimNext(priority).get();
        assertEquals("c1", first.id());

        // 청소 중에 더 급한 계산 작업이 들어왔다
        board.add(new Task("o1", Kind.CHECKOUT));
        board.complete(first);

        // 남은 청소(c2)보다 계산(o1)이 먼저 선택된다
        assertEquals("o1", board.claimNext(priority).get().id());
    }

    @Test
    void twoWorkersNeverGetTheSameTask() {
        TaskBoard board = new TaskBoard();
        board.add(new Task("t1", Kind.CLEAN));
        board.add(new Task("t2", Kind.CLEAN));

        Task a = board.claimNext(priority).get();
        Task b = board.claimNext(priority).get();

        assertNotEquals(a.id(), b.id());
        // 둘 다 예약됐으므로 세 번째 작업자는 받을 게 없다
        assertTrue(board.claimNext(priority).isEmpty());
    }

    @Test
    void kindMissingFromPriorityIsNeverPicked() {
        TaskBoard board = new TaskBoard();
        board.add(new Task("r1", Kind.RESTOCK));

        // 우선순위에서 RESTOCK을 뺀 작업자는 그 일을 하지 않는다
        assertTrue(board.claimNext(List.of(Kind.CHECKOUT)).isEmpty());
    }

    @Test
    void releasedTaskCanBeClaimedAgain() {
        TaskBoard board = new TaskBoard();
        board.add(new Task("t1", Kind.CLEAN));

        Task t = board.claimNext(priority).get();
        assertTrue(board.claimNext(priority).isEmpty());

        board.release(t); // 작업자가 중단했다
        assertEquals("t1", board.claimNext(priority).get().id());
    }
}
