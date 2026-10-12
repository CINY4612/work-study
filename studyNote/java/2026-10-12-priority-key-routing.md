<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [routing, fallback, design, optional]
date: 2026-10-12
example: example/java/priority-key-routing
status: auto
-->

# 우선순위 키로 라우팅하고 없으면 버리기

> [!NOTE]
> 메시지에 여러 식별자가 올 수 있으면 우선순위대로 값이 '있는' 첫 키 하나만 골라 그 키로만 목적지를 조회한다.

> [!WARNING]
> **헷갈린 점** · 첫 키 값이 있는데 조회에 실패했다고 다음 키로 넘어가면 엉뚱한 곳으로 갈 수 있어서, 그 경우도 폴백하지 않고 버린다.

## 📖 자세히

### 왜 이렇게 동작할까
라우팅 키가 여러 개(예: 방 ID, 상담사 ID, 발신자 ID)일 때 규칙표를 두는 대신 "우선순위 순서로 훑어서 값이 있는 첫 키를 쓴다"로 단순화할 수 있다. 여기서 헷갈리는 지점은 **'값이 있다'와 '조회에 성공했다'를 구분하는 것**이다.

- 값이 없거나 빈 문자열이면 그 키는 없는 것으로 보고 다음 키로 넘어간다 (폴백).
- 값이 있으면 그 키가 이 메시지의 주인이다. 조회에 실패해도 다음 키로 넘어가지 않는다. 넘어가면 서로 다른 대상(방 vs 사람)에게 메시지가 전달될 수 있고, 장애가 조용히 묻힌다.
- 어떤 키도 없으면 예외로 터뜨리지 않고 로그만 남기고 버린다. 소켓처럼 응답 경로가 없는 곳에서는 예외보다 안전하다.
- 키 이름의 대소문자는 보내는 쪽마다 다를 수 있어 `TreeMap(String.CASE_INSENSITIVE_ORDER)`로 정규화한다.

```java
Optional<String> route(Map<String, String> message) {
    var msg = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    msg.putAll(message);                       // 대소문자 무시
    for (String key : PRIORITY) {              // callId -> agentId -> senderId
        String v = msg.get(key);
        if (v == null || v.isBlank()) continue; // 값이 없을 때만 다음 키로
        // 값이 있으면 여기서 끝: 조회 실패도 폴백하지 않고 empty 반환
        return Optional.ofNullable(nodesByKey.getOrDefault(key, Map.of()).get(v));
    }
    return Optional.empty();                   // 키가 하나도 없음 -> 버림
}
```

## 🔎 더 공부할 것

- Optional - null 대신 '없음'을 표현하는 반환 타입의 올바른 사용법
- TreeMap과 String.CASE_INSENSITIVE_ORDER - 대소문자 무시 맵의 동작
- Dead letter - 라우팅 실패 메시지를 버리지 않고 모아두는 패턴

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#routing` `#fallback` `#design` `#optional` | 2026-10-12 | [예제 보기](../../example/java/priority-key-routing) |
