package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StatusBoardTest {

    @Test
    void leaderDoesNotEraseRowWrittenByRespondingPeer() {
        StatusBoard board = new StatusBoard();
        board.recordSelf("node-2", false); // node-2가 자기 컴포넌트 장애를 직접 기록

        board.recordPeers(Map.of("node-2", true)); // 대표 입장에서는 응답이 오는 상태

        assertTrue(board.downNodes().contains("node-2"));
    }

    @Test
    void leaderRecordsUnreachablePeer() {
        StatusBoard board = new StatusBoard();

        board.recordPeers(Map.of("node-3", false));
        board.recordPeers(Map.of("node-3", false)); // 반복해도 결과 동일

        assertEquals(Set.of("node-3"), board.downNodes());
    }

    @Test
    void recoveredNodeClearsItsOwnRow() {
        StatusBoard board = new StatusBoard();
        board.recordPeers(Map.of("node-3", false));

        board.recordSelf("node-3", true); // 살아난 노드가 스스로 정상 처리

        assertTrue(board.downNodes().isEmpty());
    }
}
