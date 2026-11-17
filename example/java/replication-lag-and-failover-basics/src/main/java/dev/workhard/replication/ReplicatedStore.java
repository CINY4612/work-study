package dev.workhard.replication;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

/** Primary 1대 + Replica 1대, 비동기 복제를 흉내 낸 인메모리 저장소. */
public class ReplicatedStore {
    private final Map<String, String> primary;
    private final Map<String, String> replica = new HashMap<>();
    // 아직 Replica에 적용되지 않은 변경 기록
    private final Queue<String[]> replicationLog = new ArrayDeque<>();

    public ReplicatedStore() {
        this.primary = new HashMap<>();
    }

    private ReplicatedStore(Map<String, String> promoted) {
        this.primary = new HashMap<>(promoted);
    }

    // 쓰기는 Primary에만 반영하고 바로 성공을 돌려준다 (Replica 반영을 기다리지 않음)
    public void write(String key, String value) {
        primary.put(key, value);
        replicationLog.add(new String[] {key, value});
    }

    public Optional<String> readFromPrimary(String key) {
        return Optional.ofNullable(primary.get(key));
    }

    // Replica는 복제 로그를 적용한 만큼만 안다
    public Optional<String> readFromReplica(String key) {
        return Optional.ofNullable(replica.get(key));
    }

    public void replicateAll() {
        String[] change;
        while ((change = replicationLog.poll()) != null) {
            replica.put(change[0], change[1]);
        }
    }

    public int lag() {
        return replicationLog.size();
    }

    // 승격: Replica에 있던 데이터만 새 Primary로 넘어가고, 로그에 남은 변경은 유실된다
    public ReplicatedStore failover() {
        return new ReplicatedStore(replica);
    }
}
