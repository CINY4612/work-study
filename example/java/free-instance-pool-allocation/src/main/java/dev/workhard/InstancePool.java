package dev.workhard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// 단일 스레드 기준의 단순화된 풀. 실제 저장소에서는 allocate 전체를 원자적으로 실행해야 한다.
public class InstancePool {
    private final Map<String, Long> freeSince = new HashMap<>();      // 빈 인스턴스 -> 비어진 시각
    private final Map<String, String> roomToInstance = new HashMap<>(); // 룸 -> 배정된 인스턴스
    private final Set<String> alive = new HashSet<>();                 // heartbeat가 살아 있는 인스턴스

    public void register(String id, long now) {
        freeSince.put(id, now);
        alive.add(id);
    }

    public void markDead(String id) {
        alive.remove(id);
    }

    public Optional<String> allocate(String roomId) {
        String existing = roomToInstance.get(roomId);
        if (existing != null) {
            return Optional.of(existing); // 재접속: 상태와 관계없이 같은 인스턴스
        }
        Optional<String> pick = freeSince.entrySet().stream()
                .filter(e -> alive.contains(e.getKey()))
                .min(Map.Entry.<String, Long>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey);
        pick.ifPresent(id -> {
            freeSince.remove(id);
            roomToInstance.put(roomId, id);
        });
        return pick;
    }

    public void release(String roomId, long now) {
        String id = roomToInstance.remove(roomId);
        if (id != null) {
            freeSince.put(id, now); // 해제 시각이 새 점수가 되어 맨 뒤로 간다
        }
    }
}
