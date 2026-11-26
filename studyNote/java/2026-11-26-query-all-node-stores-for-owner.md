<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [active-standby, distributed-state, lookup, fail-safe]
date: 2026-11-26
example: example/java/query-all-node-stores-for-owner
status: auto
-->

# 노드별 저장소에 나뉜 상태는 모든 노드를 조회

> [!NOTE]
> 상태가 노드마다 독립 저장소에 나뉘어 있으면 내 저장소만 보면 다른 노드에 붙은 대상을 못 찾으니, 모든 노드 저장소를 조회해 어느 노드 소유인지 판단한다.

> [!WARNING]
> **헷갈린 점** · 조회가 실패한 노드는 '없음'이 아니라 '알 수 없음'이다. 둘을 섞으면 살아 있는 대상을 없다고 오판한다.

## 📖 자세히

### 왜 내 저장소만 보면 안 될까
Active-Standby 구성에서 요청 처리는 Active 한 대가 하지만, 사용자(작업자)는 어느 서버에든 접속할 수 있다. 상태 저장소(예: Redis, 메모리 캐시)가 서버마다 따로 있으면 작업자의 상태는 **접속한 서버의 저장소에만** 기록된다. Active가 자기 저장소만 조회하면 상대 서버에 붙은 작업자는 항상 '없음'으로 보인다.

해결은 두 가지다.
1. 저장소를 하나로 공유한다 (장애 지점이 하나 늘어난다).
2. 저장소는 독립으로 두고, 조회하는 쪽이 **모든 노드의 저장소를 읽어** 상태가 있는 노드를 그 작업자의 소유 노드로 본다. 이때 다른 서버로의 네트워크 연결(방화벽 포트 허용 포함)이 새로 필요해진다.

### 조회 실패를 구분해야 하는 이유
상대 저장소가 죽었을 때 예외를 삼키고 '없음'으로 처리하면, 그 서버에 붙은 작업자는 존재하지 않는 사람이 된다. 그래서 결과에 `unreachable` 목록을 함께 돌려주어 호출자가 '못 찾음'과 '확인 못 함'을 다르게 처리하게 한다.

```java
public LookupResult find(String workerId) {
    List<String> unreachable = new ArrayList<>();
    for (var e : stores.entrySet()) {            // 모든 노드 저장소를 순회
        try {
            var status = e.getValue().get(workerId);
            if (status.isPresent()) {            // 상태가 있는 노드 = 소유 노드
                return new LookupResult(Optional.of(new Found(e.getKey(), status.get())), unreachable);
            }
        } catch (RuntimeException ex) {
            unreachable.add(e.getKey());         // 실패는 '없음'이 아니라 '확인 불가'로 기록
        }
    }
    return new LookupResult(Optional.empty(), unreachable);
}
```

## 🔎 더 공부할 것

- Redis 연결 타임아웃 - 상대 저장소가 느릴 때 조회 전체가 막히지 않게 하는 방법
- 병렬 조회(CompletableFuture) - 노드가 늘어날 때 순차 조회의 지연
- 단일 소유 노드 보장 - 같은 대상이 두 저장소에 남았을 때의 처리

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#active-standby` `#distributed-state` `#lookup` `#fail-safe` | 2026-11-26 | [예제 보기](../../example/java/query-all-node-stores-for-owner) |
