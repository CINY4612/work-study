package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.workhard.ReconnectClient.Reconnect;
import org.junit.jupiter.api.Test;

class ReconnectClientTest {
    private final ReconnectClient client = new ReconnectClient();
    private static final String ENTRY = "https://entry.example/main";

    @Test
    void nodeDownGoesBackThroughEntry() {
        Reconnect msg = new Reconnect("NODE_DOWN", "");
        assertEquals(ENTRY, client.destination(msg, ENTRY).orElseThrow());
    }

    @Test
    void explicitTargetWins() {
        Reconnect msg = new Reconnect("MOVE", "https://node2.example");
        assertEquals("https://node2.example", client.destination(msg, ENTRY).orElseThrow());
    }

    @Test
    void unknownReasonWithoutTargetIsIgnored() {
        Reconnect msg = new Reconnect("SOMETHING", "");
        assertTrue(client.destination(msg, ENTRY).isEmpty());
    }
}
