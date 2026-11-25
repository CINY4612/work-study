package dev.workhard.assignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoryRouter {
    private final Set<String> allNodes;
    private volatile Map<String, Set<String>> assignments = Map.of();

    public CategoryRouter(Set<String> allNodes) {
        this.allNodes = Set.copyOf(allNodes);
    }

    /** 배정표를 통째로 교체한다. 읽는 쪽은 이전 표 또는 새 표 하나만 본다. */
    public void replaceAssignments(Map<String, Set<String>> next) {
        Map<String, Set<String>> copy = new HashMap<>();
        next.forEach((k, v) -> copy.put(k, Set.copyOf(v)));
        assignments = Map.copyOf(copy);
    }

    public List<String> candidates(String category, Set<String> healthy) {
        Set<String> base = assignments.getOrDefault(category, allNodes); // 미배정 = 전체
        return base.stream().filter(healthy::contains).sorted().toList();
    }

    /** 서버 하나에만 묶인 카테고리: 그 서버가 죽으면 처리할 곳이 없다. */
    public boolean isSinglePointOfFailure(String category) {
        Set<String> assigned = assignments.get(category);
        return assigned != null && assigned.size() == 1;
    }
}
