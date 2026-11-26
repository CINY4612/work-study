package dev.workhard.lookup;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ClusterStatusLookupTest {

    private static StatusStore memory(Map<String, String> data) {
        return id -> Optional.ofNullable(data.get(id));
    }

    private static final StatusStore DOWN = id -> {
        throw new IllegalStateException("connection refused");
    };

    @Test
    void ownStoreOnlyMissesWorkerOnOtherNode() {
        var own = memory(Map.of());
        var other = memory(Map.of("w1", "READY"));

        // 내 저장소만 보면 못 찾는다
        assertTrue(new ClusterStatusLookup().addNode("A", own).find("w1").found().isEmpty());

        // 모든 노드를 보면 B 소유로 찾는다
        var r = new ClusterStatusLookup().addNode("A", own).addNode("B", other).find("w1");
        assertEquals("B", r.found().orElseThrow().nodeId());
        assertEquals("READY", r.found().get().status());
    }

    @Test
    void unreachableNodeIsReportedNotTreatedAsAbsent() {
        var r = new ClusterStatusLookup()
                .addNode("A", memory(Map.of()))
                .addNode("B", DOWN)
                .find("w1");
        assertTrue(r.found().isEmpty());
        assertEquals(java.util.List.of("B"), r.unreachable()); // '없음'이 아니라 '확인 불가'
    }

    @Test
    void foundOnHealthyNodeEvenIfAnotherIsDown() {
        var r = new ClusterStatusLookup()
                .addNode("A", DOWN)
                .addNode("B", memory(Map.of("w1", "BUSY")))
                .find("w1");
        assertEquals("B", r.found().orElseThrow().nodeId());
        assertEquals(java.util.List.of("A"), r.unreachable());
    }
}
