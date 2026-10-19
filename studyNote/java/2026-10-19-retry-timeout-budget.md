<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [timeout, retry, health-check, validation]
date: 2026-10-19
example: example/java/retry-timeout-budget
status: auto
-->

# 재시도 횟수 × 타임아웃을 주기 안에 맞추기

> [!NOTE]
> 주기적 점검에서 한 번에 최대 N회 재시도하면 최악의 소요 시간은 '타임아웃 × N'이고, 이 값이 주기보다 짧아야 사이클이 겹치지 않는다.

> [!WARNING]
> **헷갈린 점** · 호출 쪽 타임아웃은 상대가 내부에서 쓰는 타임아웃보다 커야 한다. 아니면 상대가 정상 응답하기 전에 우리가 먼저 실패로 본다.

## 📖 자세히

### 왜 계산해야 할까

주기 작업(예: 5초마다 상태 점검)은 '한 사이클이 다음 사이클 시작 전에 끝난다'는 가정 위에서 동작한다. 실패 시 재시도를 넣으면 최악의 경우 시간이 `타임아웃 × 시도 횟수`로 늘어난다. 이 값이 주기를 넘으면 사이클이 밀리거나 겹쳐서, 판정이 늦어지고 스레드가 쌓인다.

- 평소(정상 응답)에는 빨리 끝나기 때문에 문제가 드러나지 않는다. 상대가 응답하지 않는 장애 상황에서만 최악의 시간이 나온다. 그래서 값을 정할 때 미리 계산해 두어야 한다.
- 여러 대상을 병렬로 점검하면 대상 수가 아니라 가장 느린 대상 하나가 사이클 길이를 정한다.
- 단계가 중첩된 호출(A가 B를 호출하고 B가 내부에서 다시 프로브)이라면, A의 타임아웃은 B의 내부 타임아웃보다 커야 한다. 작으면 B가 정상이어도 A가 먼저 타임아웃으로 실패 처리한다.

### 설정 값을 코드로 검증하기

문서에 적어 두기만 하면 누군가 값을 바꿀 때 깨진다. 생성 시점(또는 설정 바인딩 테스트)에서 검증하면 잘못된 조합이 바로 드러난다.

```java
public record TimeoutBudget(long intervalMs, long probeTimeoutMs,
                            long callTimeoutMs, int attempts) {
    public TimeoutBudget {
        // 호출 타임아웃이 상대의 내부 프로브 타임아웃보다 커야 한다
        if (callTimeoutMs <= probeTimeoutMs) {
            throw new IllegalArgumentException("callTimeout must be > probeTimeout");
        }
        // 최악의 경우(전부 타임아웃)에도 주기 안에 끝나야 한다
        if (callTimeoutMs * attempts >= intervalMs) {
            throw new IllegalArgumentException("worst case exceeds interval");
        }
    }

    public long worstCaseMs() { return callTimeoutMs * attempts; }
}
```

예: 주기 5000ms, 프로브 1000ms, 호출 1500ms, 3회라면 최악 4500ms로 통과한다. 호출 타임아웃이 3000ms면 9000ms라서 거부된다.

### 타임아웃을 하나로 공유하지 않기

여러 용도가 한 타임아웃 설정을 같이 쓰면, 한 곳에 맞추려고 값을 바꿀 때 다른 곳의 동작도 바뀐다. 예산이 빡빡한 호출은 전용 타임아웃(설정, RestTemplate/클라이언트)을 따로 두는 편이 안전하다.

## 🔎 더 공부할 것

- Resilience4j Retry/TimeLimiter - 재시도와 타임아웃을 라이브러리로 조합하는 방법
- connect timeout vs read timeout - 두 타임아웃이 각각 어느 구간을 끊는지
- 재시도 시 지터(jitter)와 지수 백오프 - 동시에 몰리는 재시도를 분산하기
- @ConfigurationProperties 검증 - 설정 바인딩 단계에서 잘못된 조합을 막기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#timeout` `#retry` `#health-check` `#validation` | 2026-10-19 | [예제 보기](../../example/java/retry-timeout-budget) |
