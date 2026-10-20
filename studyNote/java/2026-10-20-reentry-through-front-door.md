<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [design, reconnect, single-entry, simplicity]
date: 2026-10-20
example: example/java/reentry-through-front-door
status: auto
-->

# 장애 시 노드 선정 대신 정문으로 재진입

> [!NOTE]
> 노드가 죽었을 때 알림 쪽에서 새 노드를 고르지 말고, 평소 진입 경로(정문)로 다시 들어오게 하면 선정 로직이 한 곳에만 남는다.

> [!WARNING]
> **헷갈린 점** · 알림에 목적지가 없으면 옛 클라이언트는 무시한다. 서버와 화면을 함께 배포하지 않으면 동작하지 않는다.

## 📖 자세히

### 왜 이렇게 동작할까
장애 알림 코드가 서비스 조회, 후보 계산, 최소 부하 선택을 따로 하면 정상 진입 경로의 로직과 같은 규칙을 두 군데에서 관리하게 된다. 두 곳의 결과가 어긋나기 쉽다.

대신 알림은 이유(reason)만 전달하고, 클라이언트가 정상 진입 주소로 다시 들어가게 하면 정문의 기존 로직(장애 노드 제외, 배정 실패 안내)을 그대로 재사용한다.

```java
record Reconnect(String reason, String targetUrl) {}

Optional<String> destination(Reconnect msg, String entryUrl) {
    if (!msg.targetUrl().isBlank()) return Optional.of(msg.targetUrl()); // 서버가 지정한 목적지
    if ("NODE_DOWN".equals(msg.reason())) return Optional.of(entryUrl);   // 정문으로 재진입
    return Optional.empty();                                                // 모르는 알림은 무시
}
```

### 주의점
- 정문 주소가 죽은 노드를 빼 주는 로드밸런서나 입구 서버를 거쳐야 한다. 그렇지 않으면 재진입 자체가 실패한다.
- 목적지가 비어 있으면 무시하는 옛 클라이언트가 있을 수 있다. 서버와 클라이언트를 같이 배포해야 한다.
- 단순해진 대신 정문 로직의 부하와 실패가 재진입 경로에 그대로 반영된다.

## 🔎 더 공부할 것

- Single entry point - 진입 경로를 하나로 두는 설계
- Graceful degradation - 일부 장애에도 서비스를 이어가는 방식
- 로드밸런서 헬스체크 - 죽은 노드를 입구에서 빼는 방법

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#design` `#reconnect` `#single-entry` `#simplicity` | 2026-10-20 | [예제 보기](../../example/java/reentry-through-front-door) |
