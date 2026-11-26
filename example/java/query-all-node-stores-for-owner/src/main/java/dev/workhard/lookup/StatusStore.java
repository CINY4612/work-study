package dev.workhard.lookup;

import java.util.Optional;

// 노드 하나가 가진 상태 저장소. 연결 실패는 RuntimeException으로 던진다.
public interface StatusStore {
    Optional<String> get(String workerId);
}
