package dev.workhard;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ReconnectNotifier {

    public enum Result { SENT, BUSY }

    public interface Sender {
        Result send(String userId);
    }

    private final Set<String> targets = new LinkedHashSet<>();
    private final Sender sender;

    public ReconnectNotifier(Sender sender) {
        this.sender = sender;
    }

    public void start(Collection<String> ids) {
        targets.addAll(ids);
    }

    // 주기마다 호출: 성공한 대상만 빠지고 BUSY는 남는다
    public void runCycle() {
        targets.removeIf(id -> sender.send(id) == Result.SENT);
    }

    // 원인이 해소되면 남은 대상을 버린다
    public void stop() {
        targets.clear();
    }

    public Set<String> targets() {
        return Set.copyOf(targets);
    }
}
