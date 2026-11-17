package dev.workhard.envelope;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvelopeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void envelopeKeepsExtraDataInsideData() throws Exception {
        JsonNode json = mapper.valueToTree(Envelope.ok(Map.of("orderId", "1")));

        assertTrue(json.path("data").has("orderId"));
        // 옛 소비자가 읽던 최상위 위치에는 더 이상 없다
        assertFalse(json.has("orderId"));
    }

    @Test
    void emptyDataIsOmitted() {
        JsonNode json = mapper.valueToTree(Envelope.fail(410, "gone"));

        assertFalse(json.has("data"));
    }

    @Test
    void anyGetterFlattensIntoTopLevel() {
        JsonNode json = mapper.valueToTree(new FlatResponse(200, "OK", Map.of("orderId", "1")));

        assertTrue(json.has("orderId"));
    }
}
