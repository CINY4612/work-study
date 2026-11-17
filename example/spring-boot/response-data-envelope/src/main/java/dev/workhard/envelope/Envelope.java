package dev.workhard.envelope;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

// 새 방식: 부가 데이터는 data 안에만 둔다. 비어 있으면 data 키가 빠진다
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Envelope(int resultCode, String resultMsg, Map<String, Object> data) {
    public static Envelope ok(Map<String, Object> data) {
        return new Envelope(200, "OK", data);
    }

    public static Envelope fail(int code, String msg) {
        return new Envelope(code, msg, Map.of());
    }
}
