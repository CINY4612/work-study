package dev.workhard.lookup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClusterStatusLookup {

    public record Found(String nodeId, String status) {}

    // unreachable: 조회에 실패해 '없음'인지 알 수 없는 노드들
    public record LookupResult(Optional<Found> found, List<String> unreachable) {}

    private final Map<String, StatusStore> stores = new LinkedHashMap<>();

    public ClusterStatusLookup addNode(String nodeId, StatusStore store) {
        stores.put(nodeId, store);
        return this;
    }

    public LookupResult find(String workerId) {
        List<String> unreachable = new ArrayList<>();
        for (var e : stores.entrySet()) {            // 내 저장소만이 아니라 모든 노드를 본다
            try {
                Optional<String> status = e.getValue().get(workerId);
                if (status.isPresent()) {            // 상태가 있는 쪽이 소유 노드
                    return new LookupResult(Optional.of(new Found(e.getKey(), status.get())), unreachable);
                }
            } catch (RuntimeException ex) {
                unreachable.add(e.getKey());         // 실패 != 없음
            }
        }
        return new LookupResult(Optional.empty(), unreachable);
    }
}
