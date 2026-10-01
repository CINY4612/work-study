<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [health-check, state, threshold, monitoring]
date: 2026-10-01
example: example/java/health-check-threshold
status: auto
-->

# 헬스체크에서 직전 상태를 기억해 연속 실패 판정과 상태 변화를 잡는 법

> [!NOTE]
> 주기 헬스체크는 직전 상태(연속 실패 횟수, 이전 UP/DOWN)를 기억해야 N회 연속 실패 판정과 상태가 바뀐 순간 감지를 할 수 있다.

> [!WARNING]
> **헷갈린 점** · 요청마다 바로 확인하는 방식과는 목적이 다르다. 즉시 확인은 현재 값만 알 뿐 변화를 알 수 없다.

## 📖 자세히

### 왜 직전 상태가 필요할까
한 번의 실패로 바로 DOWN 처리하면 일시적인 네트워크 흔들림에도 장애로 판정한다(오탐). 그래서 "연속 N회 실패하면 DOWN"처럼 임계값(threshold)을 둔다. 이 판정은 이전 결과를 알아야 가능하다.

또 DOWN으로 바뀐 순간에만 알림을 보내거나 로그를 남기려면 이전 상태와 비교해야 한다. 이게 없으면 DOWN인 동안 매 주기마다 같은 알림이 나간다.

1. 성공하면 실패 횟수를 0으로 되돌린다(간헐적 실패가 쌓이지 않게).
2. 실패 횟수가 임계값 이상이면 DOWN, 미만이면 이전 상태를 유지한다.
3. 이전 상태와 새 상태가 다를 때만 변화(Change)를 돌려준다.

```java
public Optional<Change> report(String target, boolean ok) {
    Status prev = last.getOrDefault(target, Status.UP);
    Status next;
    if (ok) {
        failures.remove(target);          // 성공 -> 연속 실패 횟수 초기화
        next = Status.UP;
    } else {
        int count = failures.merge(target, 1, Integer::sum);
        next = count >= threshold ? Status.DOWN : prev; // 임계값 전에는 상태 유지
    }
    last.put(target, next);
    // 바뀐 순간에만 값이 있다 -> 알림/로그는 이때만
    return prev == next ? Optional.empty() : Optional.of(new Change(target, prev, next));
}
```

### 주의할 점
- 이 맵들은 스케줄러 스레드 하나만 쓰는 전제다. 여러 스레드가 호출하면 ConcurrentHashMap 등으로 바꾸고 갱신을 원자적으로 해야 한다.
- 배정이나 라우팅처럼 정확한 값이 필요한 곳은 캐시된 상태에 의존하지 말고 요청 시점에 직접 확인하는 편이 안전하다. 직전 상태는 모니터링용이다.
- 읽는 곳이 없는 상태 저장은 시간이 지나면 죽은 코드가 된다. 쓰이는 이유가 판정과 변화 감지인지 확인하자.

## 🔎 더 공부할 것

- Circuit Breaker 패턴 - 연속 실패 시 호출 자체를 막는 방식(Resilience4j)
- ScheduledExecutorService - 주기 실행과 예외 발생 시 스케줄이 멈추는 동작
- ConcurrentHashMap.merge - 원자적 갱신이 필요한 이유

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#health-check` `#state` `#threshold` `#monitoring` | 2026-10-01 | [▶ 실행해보기](../../example/java/health-check-threshold) |
