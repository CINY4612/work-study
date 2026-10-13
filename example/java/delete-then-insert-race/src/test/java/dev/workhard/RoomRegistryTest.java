package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class RoomRegistryTest {

    @Test
    void sequentialRegisterKeepsOnlyLatest() {
        RoomRegistry registry = new RoomRegistry();
        registry.register("r1", "nodeA");
        registry.register("r1", "nodeB");

        assertEquals(1, registry.count("r1"));
        assertEquals(Optional.of("nodeB"), registry.findNode("r1"));
    }

    @Test
    void interleavedRegisterLeavesTwoRowsButLookupStillWorks() {
        RoomRegistry registry = new RoomRegistry();

        // 두 요청의 단계가 번갈아 실행된 상황을 직접 재현한다.
        assertEquals(0, registry.deleteByRoom("r1")); // A 삭제 (0건)
        assertEquals(0, registry.deleteByRoom("r1")); // B 삭제 (0건)
        registry.insert("r1", "nodeA");               // A 삽입
        registry.insert("r1", "nodeB");               // B 삽입

        assertEquals(2, registry.count("r1"));        // 중복이 남는다
        // 그래도 조회는 예외 없이 가장 늦게 등록된 노드를 돌려준다.
        assertEquals(Optional.of("nodeB"), registry.findNode("r1"));
    }

    @Test
    void unknownRoomIsEmpty() {
        assertEquals(Optional.empty(), new RoomRegistry().findNode("none"));
    }
}
