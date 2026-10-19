<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [health-check, ownership, idempotent, distributed]
date: 2026-10-19
example: example/java/status-row-owner-writes
status: auto
-->

# 상태 기록은 소유 노드만 지우게 하기

> [!NOTE]
> 노드 상태 행은 해당 노드가 직접 쓰고 지우고, 대표 노드는 응답 없는 노드만 기록한다. 정상 판정으로 남의 행을 지우지 않는다.

> [!WARNING]
> **헷갈린 점** · 'INSERT만 하는 MERGE'는 중복 기록은 막지만 DELETE 충돌은 못 막는다. 막아야 할 것은 삭제 권한이다.

## 📖 자세히

### 왜 문제가 될까

여러 노드가 같은 상태 테이블(장애 노드 목록)을 공유할 때, 대표 노드가 "저 노드 응답이 오네, 정상이니 행을 지우자"라고 하면 문제가 생긴다. 그 노드가 **자기 안의 컴포넌트 장애**를 직접 기록해 둔 행일 수 있기 때문이다. 응답(생존)과 정상(모든 컴포넌트 OK)은 다른 정보다.

### 쓰기 권한을 나누는 규칙

| 대상 | 쓰는 주체 | 동작 |
|---|---|---|
| 자기 노드 | 모든 노드 | 하나라도 장애면 기록, 모두 정상이면 삭제 |
| 다른 노드, 응답함 | 대표 | 건드리지 않음 |
| 다른 노드, 응답 없음 | 대표 | 기록(이미 있으면 그대로) |

복귀도 단순해진다. 살아난 노드가 자기 상태가 정상일 때 스스로 지우면 되므로, 대표가 복귀를 판정할 필요가 없다.

```java
// 자기 상태: 내가 유일한 쓰기 주체이므로 추가/삭제 모두 허용
public void recordSelf(String selfId, boolean allUp) {
    if (allUp) down.remove(selfId); else down.add(selfId);
}

// 상대 상태: 응답 없는 노드만 추가. 응답하는 노드는 삭제도 덮어쓰기도 하지 않는다
public void recordPeers(Map<String, Boolean> reachable) {
    reachable.forEach((id, ok) -> { if (!ok) down.add(id); });
}
```

Set.add는 이미 있으면 아무 일도 하지 않으므로, DB의 `WHEN NOT MATCHED THEN INSERT`만 있는 MERGE와 같은 멱등(여러 번 실행해도 결과가 같은) 동작이다.

## 🔎 더 공부할 것

- 멱등성(idempotency) - 반복 실행해도 안전한 쓰기를 설계하는 기준
- Single-writer 원칙 - 한 데이터를 쓰는 주체를 하나로 제한하면 충돌이 줄어든다
- liveness vs readiness - 살아있음과 정상 동작 가능을 구분하는 헬스체크

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#health-check` `#ownership` `#idempotent` `#distributed` | 2026-10-19 | [예제 보기](../../example/java/status-row-owner-writes) |
