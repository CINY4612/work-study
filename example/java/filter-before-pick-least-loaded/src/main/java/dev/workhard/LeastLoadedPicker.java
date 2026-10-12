package dev.workhard;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LeastLoadedPicker {

    public record Node(String id, int load) {}

    private static final Comparator<Node> BY_LOAD = Comparator.comparingInt(Node::load);

    // 올바른 방식: 후보로 먼저 거른 뒤 그 안에서 최소 부하를 고른다.
    // candidateIds == null 이면 제한 없음(전체), 빈 집합이면 대상 없음.
    public static Optional<Node> pickFilteredFirst(List<Node> alive, Collection<String> candidateIds) {
        return alive.stream()
                .filter(n -> candidateIds == null || candidateIds.contains(n.id()))
                .min(BY_LOAD);
    }

    // 잘못된 방식: 전체 1등을 먼저 고르면, 1등이 범위 밖일 때 범위 안의 차선을 놓친다.
    public static Optional<Node> pickThenCheck(List<Node> alive, Collection<String> candidateIds) {
        return alive.stream()
                .min(BY_LOAD)
                .filter(n -> candidateIds == null || candidateIds.contains(n.id()));
    }
}
