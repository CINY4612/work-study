package dev.workhard.priority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TaskBoard {

    public enum Kind { CHECKOUT, CLEAN, RESTOCK }

    public record Task(String id, Kind kind) {}

    private final List<Task> pending = new ArrayList<>();
    // 누군가 이미 잡은 작업 id
    private final Set<String> claimed = new HashSet<>();

    public synchronized void add(Task task) {
        pending.add(task);
    }

    // 우선순위 순서대로 훑어 아직 아무도 잡지 않은 첫 작업을 예약하고 돌려준다.
    // 고르기와 예약이 한 synchronized 구간이라 두 작업자가 같은 작업을 받을 수 없다.
    public synchronized Optional<Task> claimNext(List<Kind> priority) {
        for (Kind kind : priority) {
            for (Task t : pending) {
                if (t.kind() == kind && !claimed.contains(t.id())) {
                    claimed.add(t.id());
                    return Optional.of(t);
                }
            }
        }
        return Optional.empty();
    }

    // 작업 완료: 목록에서 제거하고 예약도 푼다
    public synchronized void complete(Task task) {
        pending.remove(task);
        claimed.remove(task.id());
    }

    // 작업 중단: 작업은 남기고 예약만 풀어 다른 작업자가 잡게 한다
    public synchronized void release(Task task) {
        claimed.remove(task.id());
    }
}
