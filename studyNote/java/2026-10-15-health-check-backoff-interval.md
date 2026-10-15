<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [backoff, health-check, retry, clock]
date: 2026-10-15
example: example/java/health-check-backoff-interval
status: auto
-->

# DOWN이 이어질 때 헬스체크 간격을 지수로 늘리기

> [!NOTE]
> 연속 실패마다 확인 간격을 2배씩 늘리고 상한을 두면, 죽은 대상에 쏟는 요청과 로그를 줄이면서 복구도 늦지 않게 감지한다.

> [!WARNING]
> **헷갈린 점** · 성공하면 실패 횟수를 0으로 되돌려 기본 간격으로 복귀해야 한다. 시프트 연산은 상한을 두지 않으면 오버플로한다.

## 📖 자세히

### 왜 간격을 늘릴까
대상이 죽어 있는 동안 매 5초마다 계속 확인하면 의미 없는 요청과 에러 로그만 쌓인다. 실패할수록 간격을 **지수(2^n)로 늘리되 상한(max)**을 두면, 초반에는 빠르게 재확인하고 길어질수록 부담을 줄인다. 상한이 없으면 복구되어도 한참 뒤에야 감지한다.

### 구현 포인트
- 간격 = `base * 2^연속실패횟수`, 단 `max`를 넘지 않는다.
- `1L << failures`는 failures가 커지면 오버플로해 음수나 0이 된다. 횟수를 20 정도로 잘라서 계산한다.
- 시간은 `Instant`를 인자로 받아 테스트에서 시계를 흉내 낸다.
- 성공하면 `failures = 0`으로 초기화해 기본 간격으로 돌아온다.

```java
public Duration currentInterval() {
    int shift = Math.min(failures, 20);            // 오버플로 방지
    long millis = base.toMillis() << shift;        // base * 2^failures
    return Duration.ofMillis(Math.min(millis, max.toMillis())); // 상한 적용
}

public void record(boolean success, Instant now) {
    failures = success ? 0 : failures + 1;         // 성공하면 초기화
    nextAt = now.plus(currentInterval());          // 다음 확인 시각 예약
}
```

### 주의
무한 증가를 막는 상한과 함께 보통 **지터(jitter, 약간의 무작위 편차)**를 더한다. 여러 노드가 같은 시각에 동시에 재시도하는 쏠림을 피하기 위해서다.

## 🔎 더 공부할 것

- jitter - 재시도 시점을 흩뜨려 동시 폭주 피하기
- Resilience4j retry/circuit breaker - 서킷 브레이커와 백오프의 관계
- java.time.Clock - 시간을 주입해 테스트하기 (https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html)

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#backoff` `#health-check` `#retry` `#clock` | 2026-10-15 | [예제 보기](../../example/java/health-check-backoff-interval) |
