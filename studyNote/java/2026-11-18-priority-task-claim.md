<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [concurrency, priority, claim, synchronized]
date: 2026-11-18
example: example/java/priority-task-claim
status: auto
-->

# 우선순위로 작업을 고르고 한 명만 잡게 하기

> [!NOTE]
> 여러 작업자가 같은 작업 보드에서 고를 때는 우선순위 순으로 훑되, 고르는 순간 '예약(claim)'을 걸어 중복 처리를 막는다.

> [!WARNING]
> **헷갈린 점** · 우선순위는 작업 하나를 끝낸 뒤 매번 다시 평가해야 낮은 순위 일을 하던 중에도 급한 일로 갈아탄다.

## 📖 자세히

### 왜 이렇게 동작할까
작업자가 둘 이상이면 '조회 후 처리' 사이에 틈이 생긴다. 둘이 같은 작업을 동시에 보면 둘 다 그 일을 시작한다. 그래서 **고르는 것과 예약하는 것을 한 번에(원자적으로)** 해야 한다. 아래 코드는 `synchronized`로 묶어서 이를 보장한다.

1. 작업자가 `claimNext(우선순위)`를 호출한다.
2. 우선순위 목록 순서대로, 아직 예약되지 않은 첫 작업을 찾는다.
3. 찾으면 예약 집합에 id를 넣고 돌려준다 (다른 작업자는 이 작업을 건너뛴다).
4. 끝나면 `complete`, 중단하면 `release`로 예약을 푼다.
5. 다음 작업은 다시 1번부터 한다. 이때 우선순위가 매번 재평가된다.

```java
// 고르기 + 예약을 하나의 synchronized 구간에서 처리
public synchronized Optional<Task> claimNext(List<Kind> priority) {
    for (Kind kind : priority) {            // 우선순위 순서대로
        for (Task t : pending) {
            if (t.kind() == kind && !claimed.contains(t.id())) {
                claimed.add(t.id());        // 여기서 예약해야 중복이 없다
                return Optional.of(t);
            }
        }
    }
    return Optional.empty();
}
```

### 헷갈리기 쉬운 점
- 우선순위를 '작업자 시작 시점에 한 번만' 정하면, 낮은 순위 일을 오래 하는 동안 급한 일이 쌓인다. 작업 단위로 쪼개 매번 다시 고르게 한다.
- 우선순위 목록에 없는 종류는 '끄기'와 같다. 목록에서 빼기만 하면 그 작업자는 그 일을 하지 않는다.
- `release`를 빼먹으면 중단된 작업이 영원히 예약 상태로 남는다.

## 🔎 더 공부할 것

- ConcurrentHashMap.newKeySet - synchronized 대신 동시성 컬렉션으로 예약 집합을 만들 수 있다
- PriorityBlockingQueue - 우선순위 큐를 스레드 안전하게 쓰는 표준 방법
- 작업 만료(lease) - 작업자가 죽었을 때 예약이 풀리게 하는 방식

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#concurrency` `#priority` `#claim` `#synchronized` | 2026-11-18 | [예제 보기](../../example/java/priority-task-claim) |
