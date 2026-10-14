package dev.workhard.ledger;

import java.util.HashMap;
import java.util.Map;

/** 자원별 합계와 소유자별 내역을 함께 관리한다. */
public class ConnectionLedger {
    private final Map<String, Integer> total = new HashMap<>();
    private final Map<String, Map<String, Integer>> byOwner = new HashMap<>();

    // 합계와 내역은 항상 같이 바꿔야 하므로 같은 락(synchronized) 안에서 갱신한다
    public synchronized void allocate(String owner, String resource) {
        total.merge(resource, 1, Integer::sum);
        byOwner.computeIfAbsent(owner, k -> new HashMap<>()).merge(resource, 1, Integer::sum);
    }

    public synchronized void release(String owner, String resource) {
        Map<String, Integer> held = byOwner.get(owner);
        // 내역에 없는 release는 무시해서 합계가 음수가 되지 않게 한다
        if (held == null || held.getOrDefault(resource, 0) == 0) return;
        held.merge(resource, -1, Integer::sum);
        total.merge(resource, -1, Integer::sum);
    }

    // owner가 죽었을 때: 그 owner가 잡고 있던 만큼만 합계에서 뺀다
    public synchronized void releaseAll(String owner) {
        Map<String, Integer> held = byOwner.remove(owner);
        if (held == null) return; // 멱등
        held.forEach((resource, count) -> total.merge(resource, -count, Integer::sum));
    }

    public synchronized int total(String resource) {
        return total.getOrDefault(resource, 0);
    }
}
