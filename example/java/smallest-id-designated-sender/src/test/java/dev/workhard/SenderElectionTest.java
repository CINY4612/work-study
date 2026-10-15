package dev.workhard;

import static dev.workhard.SenderElection.State.DOWN;
import static dev.workhard.SenderElection.State.UNKNOWN;
import static dev.workhard.SenderElection.State.UP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.workhard.SenderElection.State;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SenderElectionTest {

    // 살아있는(DOWN이 아닌) 각 노드가 자기 시점에서 담당인지 계산해 담당 노드 목록을 돌려준다
    private List<Integer> designatedNodes(Map<Integer, State> truth) {
        return truth.entrySet().stream()
            .filter(e -> e.getValue() != DOWN)
            .map(Map.Entry::getKey)
            .filter(self -> {
                Map<Integer, State> peers = new HashMap<>(truth);
                peers.remove(self);
                return SenderElection.isDesignated(self, peers);
            })
            .sorted()
            .toList();
    }

    @Test
    void onlySmallestAliveNodeSends() {
        Map<Integer, State> truth = Map.of(1, UP, 2, UP, 3, DOWN, 4, UP);
        assertEquals(List.of(1), designatedNodes(truth));
    }

    @Test
    void nextNodeTakesOverWhenSmallestIsDown() {
        Map<Integer, State> truth = Map.of(1, DOWN, 2, UP, 3, DOWN, 4, UP);
        assertEquals(List.of(2), designatedNodes(truth));
    }

    @Test
    void unknownPeerIsStillACandidate() {
        // 2번은 1번이 아직 판정 전이라 양보한다 (중복보다 잠깐의 누락을 택함)
        assertFalse(SenderElection.isDesignated(2, Map.of(1, UNKNOWN, 3, UP)));
        assertTrue(SenderElection.isDesignated(1, Map.of(2, UP, 3, UP)));
    }
}
