<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [counter, ledger, cleanup, concurrency]
date: 2026-10-14
example: example/java/per-owner-allocation-ledger
status: auto
-->

# 소유자별 배정 내역으로 죽은 노드의 카운트 정리

> [!NOTE]
> 자원별 합계 카운터만 두면, 소유자(요청 노드)가 죽어도 얼마를 줄일지 알 수 없어 카운트가 영영 남는다.

> [!WARNING]
> **헷갈린 점** · 소유자별 건수를 함께 기록하면 소유자가 사라질 때 그 몫만 합계에서 정확히 뺄 수 있다.

## 📖 자세히

### 왜 합계만으로는 안 될까

연결 수를 `daemon -> 개수` 합계로만 저장하면, 배정을 요청한 쪽(owner)이 정상적으로 `release`를 호출할 때만 숫자가 줄어듭니다. owner가 비정상 종료하면 release가 오지 않고, 합계에는 "누가 몇 개를 잡았는지"가 없어서 대신 정리할 수도 없습니다. 그 자원은 실제보다 부하가 높게 보여 배정에서 계속 밀립니다.

해결은 합계와 함께 **owner별 내역(원장, ledger)** 을 같은 락 안에서 갱신하는 것입니다. 합계와 내역이 어긋나면 안 되므로 두 맵을 한 메서드에서 함께 바꾸고 `synchronized`로 묶습니다.

```java
public synchronized void releaseAll(String owner) {
    Map<String, Integer> held = byOwner.remove(owner);
    if (held == null) return; // 두 번 호출해도 안전(멱등)
    // owner가 잡고 있던 만큼만 합계에서 뺀다
    held.forEach((daemon, count) -> total.merge(daemon, -count, Integer::sum));
}
```

정상 경로의 `release`가 내역도 같이 차감하므로 나중에 `releaseAll`이 이중으로 빼지 않습니다. 이미 0인 건을 또 release해도 음수가 되지 않게 막는 것이 포인트입니다. 대신 저장할 상태가 하나 늘어나는 비용이 있습니다.

## 🔎 더 공부할 것

- 멱등성(idempotency) - 정리 작업을 여러 번 호출해도 결과가 같게 만들기
- lease/TTL 방식 - 내역을 저장하는 대신 만료 시각으로 자동 회수하는 대안
- ConcurrentHashMap.merge - 락 없이 원자적으로 카운트 갱신하기
- https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html - Map.merge 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#counter` `#ledger` `#cleanup` `#concurrency` | 2026-10-14 | [예제 보기](../../example/java/per-owner-allocation-ledger) |
