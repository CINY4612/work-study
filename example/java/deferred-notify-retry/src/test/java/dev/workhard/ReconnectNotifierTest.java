package dev.workhard;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReconnectNotifierTest {

    private final Set<String> busy = new HashSet<>();
    private final ReconnectNotifier notifier = new ReconnectNotifier(
            id -> busy.contains(id) ? ReconnectNotifier.Result.BUSY : ReconnectNotifier.Result.SENT);

    @Test
    void busyTargetStaysUntilItBecomesFree() {
        busy.add("b");
        notifier.start(List.of("a", "b"));

        notifier.runCycle();
        assertEquals(Set.of("b"), notifier.targets()); // a는 성공, b는 보류

        busy.clear();
        notifier.runCycle();
        assertTrue(notifier.targets().isEmpty()); // 다음 주기에 처리됨
    }

    @Test
    void stopDiscardsRemainingTargets() {
        busy.add("b");
        notifier.start(List.of("b"));
        notifier.runCycle();
        notifier.stop();
        assertTrue(notifier.targets().isEmpty());
    }
}
