package dev.workhard;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class PriorityKeyRouter {

    // 앞에 있을수록 우선순위가 높다
    static final List<String> PRIORITY = List.of("callId", "agentId", "senderId");

    // 키 이름 -> (키 값 -> 목적지 노드)
    private final Map<String, Map<String, String>> nodesByKey;

    public PriorityKeyRouter(Map<String, Map<String, String>> nodesByKey) {
        this.nodesByKey = nodesByKey;
    }

    /** 목적지를 찾으면 노드를, 못 찾거나 버려야 하면 empty를 돌려준다. */
    public Optional<String> route(Map<String, String> message) {
        // 보내는 쪽마다 대소문자가 다를 수 있어 대소문자 무시 맵으로 복사
        var msg = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        msg.putAll(message);

        for (String key : PRIORITY) {
            String value = msg.get(key);
            if (value == null || value.isBlank()) {
                continue; // 값이 없을 때만 다음 우선순위로 폴백
            }
            // 값이 있으면 이 키로 확정. 조회에 실패해도 다음 키로 넘어가지 않는다
            return Optional.ofNullable(nodesByKey.getOrDefault(key, Map.of()).get(value));
        }
        return Optional.empty(); // 키가 하나도 없으면 버린다
    }
}
