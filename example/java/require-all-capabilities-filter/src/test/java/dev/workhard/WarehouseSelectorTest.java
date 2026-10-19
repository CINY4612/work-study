package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.workhard.WarehouseSelector.Warehouse;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WarehouseSelectorTest {

    private final List<Warehouse> warehouses = List.of(
            new Warehouse(1, Set.of("STANDARD")),
            new Warehouse(2, Set.of("STANDARD", "COLD")));

    @Test
    void coldOrderGoesOnlyToWarehouseThatHandlesCold() {
        // 기본 유형은 빼고 COLD만 필요 -> 창고 2만 통과
        assertEquals(List.of(2), WarehouseSelector.candidateIds(warehouses, Set.of("STANDARD", "COLD")));
    }

    @Test
    void anyMatchWronglyKeepsWarehouseWithoutCold() {
        // 비교용: any 방식은 STANDARD만 겹쳐도 창고 1이 섞인다.
        assertEquals(List.of(1, 2), WarehouseSelector.candidateIdsAny(warehouses, Set.of("STANDARD", "COLD")));
    }

    @Test
    void standardOnlyOrEmptyFallsBackToDefault() {
        assertEquals(List.of(1, 2), WarehouseSelector.candidateIds(warehouses, Set.of("STANDARD")));
        assertEquals(List.of(1, 2), WarehouseSelector.candidateIds(warehouses, Set.of()));
    }

    @Test
    void noWarehouseHandlesAllRequiredTypes() {
        // 전부 처리하는 창고가 없으면 빈 목록 -> 호출하는 쪽이 '후보 없음'으로 다뤄야 한다.
        assertEquals(List.of(), WarehouseSelector.candidateIds(warehouses, Set.of("COLD", "FROZEN")));
    }
}
