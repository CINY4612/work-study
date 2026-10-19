package dev.workhard;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** 장애 노드 목록. DB 테이블 대신 메모리 Set으로 대신한다. */
public class StatusBoard {

    private final Set<String> down = new TreeSet<>();

    /** 자기 상태: 내가 유일한 쓰기 주체이므로 추가/삭제 모두 허용 */
    public void recordSelf(String selfId, boolean allComponentsUp) {
        if (allComponentsUp) {
            down.remove(selfId);
        } else {
            down.add(selfId);
        }
    }

    /** 대표가 호출: 응답 없는 노드만 추가하고, 응답하는 노드는 건드리지 않는다 */
    public void recordPeers(Map<String, Boolean> reachable) {
        reachable.forEach((nodeId, ok) -> {
            if (!ok) {
                down.add(nodeId); // 이미 있으면 변화 없음 (멱등)
            }
            // ok == true 여도 삭제하지 않는다: 그 노드가 직접 쓴 장애 행일 수 있다
        });
    }

    public Set<String> downNodes() {
        return new TreeSet<>(down);
    }
}
