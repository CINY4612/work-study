<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [scheduling, retry, state]
date: 2026-10-30
example: example/java/deferred-notify-retry
status: auto
-->

# 보류된 알림 대상을 남겨 다음 주기에 재시도

> [!NOTE]
> 주기 작업에서 바쁜 대상은 대상 목록에 남겨 두고, 성공한 대상만 빼면 다음 주기에 자연스럽게 재시도된다.

> [!WARNING]
> **헷갈린 점** · 별도 재시도 큐 없이 목록 + 종료 조건(복구 시 clear)만으로 충분하다.

## 📖 자세히

### 왜 이렇게 동작할까
사용 중인 대상에게는 지금 알릴 수 없을 때가 있습니다(예: 응답 409 같은 '지금은 안 됨'). 이때 실패로 버리지 않고 대상 집합에 그대로 두면, 다음 주기에 같은 코드가 다시 시도합니다. 성공한 대상만 제거하고, 원인이 해소되면 남은 대상을 한 번에 비웁니다.

```java
public void runCycle() {
    // SENT면 제거, BUSY면 남겨서 다음 주기에 다시 시도
    targets.removeIf(id -> sender.send(id) == Result.SENT);
}

public void stop() {
    targets.clear(); // 원인 해소 시 남은 대상 버림
}
```

### 주의점
- 보류 대상이 영원히 남지 않도록 종료 조건(복구, 최대 시도 횟수)을 둡니다.
- 같은 대상에게 중복 발송돼도 괜찮은지(멱등성) 확인합니다.

## 🔎 더 공부할 것

- 멱등성 - 재시도 시 중복 발송 방지
- ScheduledExecutorService - 주기 실행과 예외 처리
- Collection.removeIf - 순회 중 안전한 제거

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#scheduling` `#retry` `#state` | 2026-10-30 | [예제 보기](../../example/java/deferred-notify-retry) |
