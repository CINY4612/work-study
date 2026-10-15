<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [leader, idempotency, distributed, notification]
date: 2026-10-15
example: example/java/smallest-id-designated-sender
status: auto
-->

# 가장 작은 id의 살아있는 노드만 발송하게 하기

> [!NOTE]
> 같은 장애를 여러 노드가 각자 감지하면 알림이 노드 수만큼 중복되므로, 살아있는 노드 중 id가 가장 작은 노드만 보내게 한다.

> [!WARNING]
> **헷갈린 점** · 락이나 리더 선출 없이도 모두가 같은 상태를 보면 같은 답이 나온다. 단 UNKNOWN을 빼면 기동 직후 여럿이 보낼 수 있다.

## 📖 자세히

### 왜 중복이 생길까
노드 1~4가 서로 헬스체크를 하면, 3번이 죽었을 때 1·2·4번이 **각자** 3번의 DOWN을 판정하고 각자 알림을 보낸다. 각 노드의 메모리(대기 목록, 발송 중 플래그)는 서로 공유되지 않으므로 다른 노드가 보냈는지 알 길이 없다. 노드 내부의 중복 방지 플래그는 **같은 노드 안**의 겹침만 막는다.

### 락 없이 담당을 정하는 규칙
- 후보 = 자기 자신 + DOWN이 아닌 상대 노드
- 후보 중 id가 가장 작은 노드가 담당
- 모든 노드가 같은 상태를 보고 있다면 같은 규칙에서 같은 담당이 나온다. 통신이 따로 필요 없다.
- 담당이 죽으면 후보에서 빠지므로 다음 id가 자동으로 이어받는다(failover).

### UNKNOWN(판정 전)을 후보에 넣는 이유
DOWN만 빼고 UNKNOWN을 후보에서 제외하면, 기동 직후에는 서로를 아직 모르므로 모두 "내가 최소"라고 믿고 같이 보낼 수 있다. UNKNOWN을 후보에 넣으면 판정이 날 때까지 아무도 안 보낼 수는 있지만 중복은 없다. **중복 vs 누락 중 무엇을 택할지**는 설계 판단이다.

```java
public static boolean isDesignated(int selfId, Map<Integer, State> peers) {
    int smallest = Stream.concat(
            Stream.of(selfId),                       // 자기 자신은 항상 후보
            peers.entrySet().stream()
                .filter(e -> e.getValue() != State.DOWN) // DOWN만 제외, UNKNOWN은 포함
                .map(Map.Entry::getKey))
        .min(Integer::compare).orElseThrow();
    return smallest == selfId;
}
```

### 한계
노드마다 보는 상태가 잠깐 다르면(네트워크 분리, 판정 시점 차이) 두 노드가 동시에 담당이라 믿을 수 있다. 그래서 수신 쪽도 멱등(같은 알림을 두 번 받아도 안전)하게 만들어 두는 것이 안전하다.

## 🔎 더 공부할 것

- 멱등성(idempotency) - 중복 알림이 와도 결과가 같게 만들기
- 리더 선출(ZooKeeper, Redis 락 등) - 규칙 기반 담당과 무엇이 다른가
- split brain - 노드끼리 보는 상태가 갈릴 때의 문제

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#leader` `#idempotency` `#distributed` `#notification` | 2026-10-15 | [예제 보기](../../example/java/smallest-id-designated-sender) |
