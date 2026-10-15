package dev.workhard;

import java.util.Map;
import java.util.stream.Stream;

public final class SenderElection {

    public enum State { UP, DOWN, UNKNOWN }

    private SenderElection() {}

    // peers: 자기 자신을 뺀 상대 노드들의 상태
    public static boolean isDesignated(int selfId, Map<Integer, State> peers) {
        int smallest = Stream.concat(
                Stream.of(selfId), // 자기 자신은 항상 후보
                peers.entrySet().stream()
                    .filter(e -> e.getValue() != State.DOWN) // DOWN만 제외, UNKNOWN은 후보에 포함
                    .map(Map.Entry::getKey))
            .min(Integer::compare)
            .orElseThrow();
        return smallest == selfId;
    }
}
