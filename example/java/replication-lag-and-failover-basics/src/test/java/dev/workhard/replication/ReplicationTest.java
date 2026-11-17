package dev.workhard.replication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.workhard.replication.Quorum.Mode;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReplicationTest {

    @Test
    void replicaCannotSeeWriteBeforeReplication() {
        ReplicatedStore store = new ReplicatedStore();
        store.write("order-1", "CREATED");

        assertEquals(Optional.of("CREATED"), store.readFromPrimary("order-1"));
        // 복제 전이라 Replica에서는 방금 쓴 값을 못 찾는다
        assertEquals(Optional.empty(), store.readFromReplica("order-1"));
        assertEquals(1, store.lag());

        store.replicateAll();
        assertEquals(Optional.of("CREATED"), store.readFromReplica("order-1"));
    }

    @Test
    void failoverLosesUnreplicatedCommit() {
        ReplicatedStore store = new ReplicatedStore();
        store.write("a", "1");
        store.replicateAll();
        store.write("b", "2"); // 커밋은 성공했지만 Replica에는 아직 없음

        ReplicatedStore promoted = store.failover();

        assertEquals(Optional.of("1"), promoted.readFromPrimary("a"));
        assertEquals(Optional.empty(), promoted.readFromPrimary("b")); // 유실
    }

    @Test
    void partitionBetweenTwoServersCausesSplitBrainInRunningMode() {
        // 선만 끊기면 양쪽 모두 자기 서버 1대만 보인다
        boolean sideA = Quorum.mayPromote(Mode.MAJORITY_OF_RUNNING, 1, 2);
        boolean sideB = Quorum.mayPromote(Mode.MAJORITY_OF_RUNNING, 1, 2);
        assertTrue(sideA && sideB); // Primary가 둘
    }

    @Test
    void serversModePreventsSplitBrainButAlsoBlocksRealFailover() {
        // 단절이든 상대 서버 사망이든 1대만 보이면 승격하지 않는다
        assertFalse(Quorum.mayPromote(Mode.MAJORITY_OF_SERVERS, 1, 2));
        // 전체가 보이면 정상 판단 가능
        assertTrue(Quorum.mayPromote(Mode.MAJORITY_OF_SERVERS, 2, 2));
    }
}
