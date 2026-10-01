package dev.workhard;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HealthTracker {

    public enum Status { UP, DOWN }

    public record Change(String target, Status from, Status to) {}

    private final int threshold;
    private final Map<String, Integer> failures = new HashMap<>(); // 연속 실패 횟수
    private final Map<String, Status> last = new HashMap<>();      // 직전 상태

    public HealthTracker(int threshold) {
        this.threshold = threshold;
    }

    public Optional<Change> report(String target, boolean ok) {
        Status prev = last.getOrDefault(target, Status.UP);
        Status next;
        if (ok) {
            failures.remove(target); // 성공하면 연속 실패 횟수 초기화
            next = Status.UP;
        } else {
            int count = failures.merge(target, 1, Integer::sum);
            next = count >= threshold ? Status.DOWN : prev; // 임계값 전에는 상태 유지
        }
        last.put(target, next);
        // 상태가 바뀐 순간에만 값이 있다
        return prev == next ? Optional.empty() : Optional.of(new Change(target, prev, next));
    }
}
