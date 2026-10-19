package dev.workhard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WarehouseSelector {

    public static final String DEFAULT_CAP = "STANDARD";

    // 창고 id와 처리 가능한 취급 유형
    public record Warehouse(int id, Set<String> capabilities) {}

    private WarehouseSelector() {}

    // 필요 조건: 기본 유형은 모든 창고가 가지므로, 다른 유형이 있으면 뺀다.
    static Set<String> requiredCapabilities(Set<String> orderCaps) {
        Set<String> required = new HashSet<>(orderCaps);
        required.remove(DEFAULT_CAP);
        // 남은 게 없으면(비었거나 기본만 있었으면) 기본 유형을 요구한다.
        return required.isEmpty() ? Set.of(DEFAULT_CAP) : required;
    }

    // 올바른 방식: 필요한 유형을 전부 처리하는 창고만 후보
    public static List<Integer> candidateIds(List<Warehouse> warehouses, Set<String> orderCaps) {
        Set<String> required = requiredCapabilities(orderCaps);
        return warehouses.stream()
                .filter(w -> w.capabilities().containsAll(required))
                .map(Warehouse::id)
                .toList();
    }

    // 잘못된 방식(비교용): 하나라도 겹치면 후보 -> 기본 유형만 겹쳐도 통과해 버린다.
    public static List<Integer> candidateIdsAny(List<Warehouse> warehouses, Set<String> orderCaps) {
        Set<String> wanted = orderCaps.isEmpty() ? Set.of(DEFAULT_CAP) : orderCaps;
        return warehouses.stream()
                .filter(w -> w.capabilities().stream().anyMatch(wanted::contains))
                .map(Warehouse::id)
                .toList();
    }
}
