<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [error-handling, side-effect, try-catch]
date: 2026-10-30
example: example/java/best-effort-side-effect
status: auto
-->

# 부가 처리 실패가 본 요청을 깨뜨리지 않게 하기

> [!NOTE]
> 알림·로그아웃 같은 부가 처리는 실패해도 예외를 삼키고 로그만 남겨, 이미 끝난 본 작업의 결과를 유지한다.

> [!WARNING]
> **헷갈린 점** · 모든 예외를 삼키는 게 아니다. 본 작업 실패는 그대로 던지고, 부가 처리만 범위를 좁혀 감싼다.

## 📖 자세히

### 왜 이렇게 동작할까
주문 저장처럼 핵심 작업이 성공한 뒤에 알림을 보내다 예외가 나면, 호출자는 '주문 실패'로 받아들여 재시도하거나 중복 주문을 만들 수 있습니다. 부가 처리(알림, 부가 로그아웃 등)는 실패해도 본 결과가 바뀌지 않으므로 그 지점에서만 예외를 잡고 로그를 남깁니다.

```java
public String place(String id) {
    String result = "PLACED:" + id;      // 본 작업은 먼저 끝낸다
    try {
        notifier.send("placed " + id);   // 부가 처리만 try로 감싼다
    } catch (RuntimeException e) {
        log.add("notify failed: " + e.getMessage()); // 삼키되 흔적은 남긴다
    }
    return result;                        // 본 결과는 그대로 반환
}
```

### 주의점
- try 범위를 넓혀 본 작업까지 감싸면 진짜 실패도 숨겨집니다.
- 로그 없이 삼키면 장애를 알 수 없으니 반드시 기록합니다.
- 부가 처리가 꼭 성공해야 하는 요구라면 이 패턴이 아니라 재시도 큐나 아웃박스를 고려합니다.

## 🔎 더 공부할 것

- Transactional Outbox - 알림을 유실 없이 보내는 방법
- @TransactionalEventListener - 커밋 후 부가 처리 분리
- Resilience4j - 부가 호출의 재시도와 서킷 브레이커

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#error-handling` `#side-effect` `#try-catch` | 2026-10-30 | [예제 보기](../../example/java/best-effort-side-effect) |
