package dev.workhard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

public class JobRouter {
    private final Map<String, String> assignment = new HashMap<>(); // jobId -> nodeId
    private final Map<String, Integer> load = new TreeMap<>();
    private final Set<String> down = new HashSet<>();

    public JobRouter(List<String> nodes) {
        nodes.forEach(n -> load.put(n, 0));
    }

    public void markDown(String node) { down.add(node); }

    public void markUp(String node) { down.remove(node); }

    /** 신규 배정에서만 헬스 정보를 참고한다. */
    public String assign(String jobId) {
        String existing = assignment.get(jobId);
        if (existing != null) return existing;
        String best = load.keySet().stream()
                .filter(n -> !down.contains(n))
                .min(Comparator.comparingInt(load::get))
                .orElseThrow(() -> new IllegalStateException("no alive node"));
        assignment.put(jobId, best);
        load.merge(best, 1, Integer::sum);
        return best;
    }

    /** 후속 요청: 헬스 정보를 보지 않고, 아무것도 지우지 않는다. */
    public String route(String jobId) {
        String node = assignment.get(jobId);
        if (node == null) throw new NoSuchElementException(jobId);
        return node;
    }

    /** 배정 정보를 지우는 유일한 곳. */
    public void finish(String jobId) {
        String node = assignment.remove(jobId);
        if (node != null) load.merge(node, -1, Integer::sum);
    }

    public int assignedCount() { return assignment.size(); }
}
