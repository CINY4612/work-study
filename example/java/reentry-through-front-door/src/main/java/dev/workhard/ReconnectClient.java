package dev.workhard;

import java.util.Optional;

public class ReconnectClient {

    // 알림: 이유와 (선택) 목적지. 장애 때는 목적지를 비워 보낸다.
    public record Reconnect(String reason, String targetUrl) {}

    public Optional<String> destination(Reconnect msg, String entryUrl) {
        if (!msg.targetUrl().isBlank()) {
            return Optional.of(msg.targetUrl()); // 서버가 노드를 지정한 경우
        }
        if ("NODE_DOWN".equals(msg.reason())) {
            return Optional.of(entryUrl); // 노드 선정은 정문에 맡기고 다시 들어간다
        }
        return Optional.empty(); // 처리할 수 없는 알림은 무시
    }
}
