package dev.workhard.cleanup;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StartupCleanupTest {

    @Test
    void globalCleanupOnRestartWipesOtherNodeSessions() {
        KeyValueStore shared = new KeyValueStore();
        AppNode a = new AppNode("A", shared, false);
        AppNode b = new AppNode("B", shared, false);
        a.login("s1");

        b.start(); // B 재시작

        assertFalse(shared.contains(a.sessionKey("s1"))); // A의 세션이 사라졌다
    }

    @Test
    void scopedCleanupKeepsOtherNodeSessions() {
        KeyValueStore shared = new KeyValueStore();
        AppNode a = new AppNode("A", shared, true);
        AppNode b = new AppNode("B", shared, true);
        a.login("s1");
        b.login("old");

        b.start(); // B는 자기 키만 지운다

        assertTrue(shared.contains(a.sessionKey("s1")));
        assertFalse(shared.contains(b.sessionKey("old")));
    }
}
