package dev.workhard.dockerps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ContainerStatusParserTest {

    private static final String OUTPUT = String.join("\r\n",
            "web-1\tUp 3 hours",
            "worker-1\tExited (1) 2 minutes ago",
            "cache-1\tRestarting (1) 5 seconds ago",
            "api-1\tUp 10 minutes (unhealthy)",
            "",
            "broken line without tab");

    @Test
    void upIsHealthyAndOthersAreNot() {
        Map<String, Boolean> states = ContainerStatusParser.parse(OUTPUT);

        assertTrue(states.get("web-1"));
        assertFalse(states.get("worker-1"));   // Exited
        assertFalse(states.get("cache-1"));    // Restarting
    }

    @Test
    void upButUnhealthyOrPausedIsDown() {
        assertFalse(ContainerStatusParser.isUp("Up 10 minutes (unhealthy)"));
        assertFalse(ContainerStatusParser.isUp("Up 2 hours (Paused)"));
        assertTrue(ContainerStatusParser.isUp("Up 2 hours (healthy)"));
    }

    @Test
    void missingContainerIsTreatedAsDown() {
        Map<String, Boolean> states = ContainerStatusParser.parse(OUTPUT);

        assertFalse(ContainerStatusParser.isHealthy(states, "not-listed"));
    }

    @Test
    void blankAndMalformedLinesAreIgnored() {
        Map<String, Boolean> states = ContainerStatusParser.parse(OUTPUT);

        assertEquals(4, states.size());
    }
}
