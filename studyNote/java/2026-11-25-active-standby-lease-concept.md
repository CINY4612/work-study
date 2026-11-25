<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [active-standby, lease, failover, high-availability]
date: 2026-11-25
example: example/java/active-standby-lease-concept
status: auto
-->

# Active-Standby 이중화와 임대 방식 개념 정리

> [!NOTE]
> Active-Standby는 같은 일을 하는 노드 둘 중 하나만 일하게 하고, 누가 Active인지는 공유 저장소의 만료 시각 있는 임대(lease)로 정한다.

> [!WARNING]
> **헷갈린 점** · 임대가 만료돼도 옛 Active는 아직 자기가 Active라고 믿을 수 있어서, 요청은 반드시 임대 보유자에게만 보내야 한다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
- **Active-Standby**: 같은 기능을 하는 노드를 둘 이상 띄우되, 한 시점에 한 노드(Active)만 요청을 처리하고 나머지(Standby)는 대기하는 구성이다.
- 왜 쓰나: 상태를 가진 컴포넌트(대기열, 배정 정보 등)를 두 노드가 동시에 처리하면 같은 요청을 중복 처리하거나 상태가 갈라진다. 한 노드만 처리하게 하면 일관성은 지키고, Active가 죽었을 때만 Standby가 넘겨받아 가용성도 얻는다.
- 문제는 "누가 Active인가"를 어떻게 정하느냐다. 노드끼리 직접 합의하는 대신, 둘 다 접근할 수 있는 공유 저장소(DB 등)에 **임대(lease)** 를 두는 방식이 구현이 단순하다.

### 동작 원리
1. 각 노드는 일정 주기마다 공유 저장소에 "내가 임대를 잡겠다"(`tryAcquire`)고 요청한다.
2. 임대가 비었거나 만료됐으면 요청한 노드가 보유자가 되고, 만료 시각을 `지금 + TTL`로 둔다.
3. 이미 다른 노드가 유효한 임대를 갖고 있으면 실패하고, 그 노드는 Standby로 남는다.
4. 보유자는 만료 전에 같은 요청을 반복해서 임대를 **갱신**한다. 이것이 생존 신호(heartbeat) 역할을 한다.
5. 보유자가 죽으면 갱신이 멈추고, TTL이 지나 임대가 만료되면 다음 `tryAcquire`를 호출한 Standby가 새 보유자가 된다.
6. 요청을 보내는 쪽(라우터)은 "현재 임대 보유자"를 조회해서 그 노드로만 보낸다.

```text
시간 0     : A tryAcquire -> 성공 (만료 10)
시간 5     : B tryAcquire -> 실패 (A가 유효)
시간 5~10  : A가 죽어서 갱신 없음
시간 10    : B tryAcquire -> 성공 (A의 임대 만료) term 2
```

### 구성 요소/용어 정리
| 용어 | 뜻 |
|---|---|
| lease(임대) | 만료 시각이 있는 "내가 Active"라는 표시 |
| TTL | 갱신 없이 임대가 유지되는 시간. 갱신 주기보다 충분히 길어야 한다 |
| 갱신 | 보유자가 만료 전에 임대를 다시 잡는 것 |
| term | 보유자가 바뀔 때마다 1씩 증가하는 번호(세대) |
| 라우터 | 임대 보유자를 조회해 요청을 보내는 쪽 |

### 코드로 보기
```java
// 임대 저장소: 시간은 주입받아 테스트에서 직접 흘려보낸다
public synchronized boolean tryAcquire(String nodeId) {
    long now = clock.getAsLong();
    boolean free = holder == null || now >= expiresAt;      // 비었거나 만료
    if (free || holder.equals(nodeId)) {                    // 새로 잡거나 갱신
        if (!nodeId.equals(holder)) term++;                 // 보유자가 바뀔 때만 term 증가
        holder = nodeId;
        expiresAt = now + ttl;
        return true;
    }
    return false;                                           // 다른 노드가 유효하게 보유 중
}
```
```java
// 라우터: Active 여부를 노드 자신이 아니라 저장소의 보유자로 판단
String holder = store.holder();       // 만료됐으면 null
if (holder == null) throw new IllegalStateException("no active node");
return nodes.get(holder).handle(request);
```

### 비교
| | Active-Standby | Active-Active |
|---|---|---|
| 처리 노드 | 한 노드 | 모든 노드 |
| 상태 일관성 | 쉽다 | 공유 상태 설계가 필요하다 |
| 자원 활용 | Standby는 놀고 있다 | 모두 사용 |
| 장애 시 | 임대 만료 후 전환(수 초 공백) | 남은 노드가 계속 처리 |

### 자주 하는 실수와 주의점
- **TTL이 너무 짧다**: 잠깐 느려진 것만으로 임대가 넘어가 전환이 잦아진다. 갱신 주기의 서너 배 이상으로 잡는다.
- **TTL이 너무 길다**: Active가 죽어도 그 시간 동안 아무도 처리하지 못한다.
- **옛 Active의 착각**: 멈췄다 돌아온 옛 노드는 `tryAcquire`를 다시 호출하기 전까지 자신이 Active라고 믿을 수 있다. 쓰기에는 term 번호를 같이 보내 저장소가 옛 번호를 거절하게 해야 한다(펜싱 토큰).
- **Standby로 직접 요청**: 라우터를 거치지 않은 요청이 Standby에 닿으면 처리하지 말고 거절한다.
- **공유 저장소가 단일 장애점**이 될 수 있다. 저장소가 죽으면 새 임대를 못 잡는다.

### 세션에서 나온 질문 정리
- **Q. Standby는 Active 상태를 어떻게 알까?**
  A. 알 필요 없이 주기적으로 임대를 시도하고, 성공하면 Active가 된다. 상태를 노드끼리 주고받지 않는다.
- **Q. 서버가 두 대인데 각 서버 안의 컴포넌트가 다른 서버의 Active 컴포넌트를 써야 하면?**
  A. 요청을 보내는 쪽이 항상 보유자 한 곳에만 보내게 하고, Standby에는 보내지 않는다. 여러 노드에 나눠 보내던 코드를 이 규칙으로 바꿔야 한다.
- **Q. Active가 죽으면 거기 있던 상태는?**
  A. 임대만 넘어가고 메모리 상태는 따라오지 않는다. 새 Active가 공유 저장소에서 다시 읽거나, 연결이 끊긴 쪽에서 정리하는 코드가 별도로 필요하다.
- **Q. 그냥 둘 다 처리하게 하면 안 되나?**
  A. 상태를 가진 처리는 중복 배정이 생긴다. 그래서 Active-Active로 가려면 상태 분리가 먼저 필요하다.

## 🔎 더 공부할 것

- 펜싱 토큰 - 옛 Active의 늦은 쓰기를 저장소가 거절하는 방법
- Leader election(Raft, ZooKeeper) - 공유 DB 임대 대신 합의 알고리즘을 쓰는 경우
- Split brain - 네트워크가 갈라질 때 Active가 둘이 되는 문제

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#active-standby` `#lease` `#failover` `#high-availability` | 2026-11-25 | [예제 보기](../../example/java/active-standby-lease-concept) |
