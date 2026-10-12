package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PriorityKeyRouterTest {

    private final PriorityKeyRouter router = new PriorityKeyRouter(Map.of(
            "callId", Map.of("c1", "node-A"),
            "agentId", Map.of("a1", "node-B"),
            "senderId", Map.of("s1", "node-C")));

    @Test
    void callIdWinsOverOtherKeys() {
        var msg = Map.of("callId", "c1", "agentId", "a1");
        assertEquals(Optional.of("node-A"), router.route(msg));
    }

    @Test
    void blankCallIdFallsBackToAgentId() {
        var msg = Map.of("callId", " ", "agentId", "a1");
        assertEquals(Optional.of("node-B"), router.route(msg));
    }

    @Test
    void unknownCallIdDoesNotFallBack() {
        // callId 값이 있으므로 agentId가 유효해도 쓰지 않고 버린다
        var msg = Map.of("callId", "unknown", "agentId", "a1");
        assertTrue(router.route(msg).isEmpty());
    }

    @Test
    void keyNamesAreCaseInsensitive() {
        assertEquals(Optional.of("node-C"), router.route(Map.of("SENDERID", "s1")));
    }

    @Test
    void noKeyMeansDrop() {
        assertTrue(router.route(Map.of("other", "x")).isEmpty());
    }
}
