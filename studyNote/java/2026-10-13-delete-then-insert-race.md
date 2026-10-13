<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [concurrency, race-condition, unique-key, idempotent]
date: 2026-10-13
example: example/java/delete-then-insert-race
status: auto
-->

# 삭제 후 등록 방식의 동시 등록 틈

> [!NOTE]
> '지우고 다시 넣기'는 순차 실행에선 최신 등록이 이기지만, 두 요청이 동시에 오면 둘 다 0건 삭제 후 삽입해 행이 2개 남을 수 있다.

> [!WARNING]
> **헷갈린 점** · PK에 노드 ID가 섞여 있으면 DB가 막아 주지 못한다. 조회는 2건이어도 터지지 않게 최신 한 건을 고르도록 방어하자.

## 📖 자세히

### 왜 이렇게 동작할까

등록을 `DELETE` -> `INSERT` 두 단계로 하면 두 단계 사이에 다른 요청이 끼어들 수 있다.

1. 요청 A: roomId=r1 삭제 (0건)
2. 요청 B: roomId=r1 삭제 (0건, A는 아직 삽입 전)
3. A 삽입, B 삽입 -> r1 행이 2개

PK가 `(roomId, nodeId)`처럼 노드를 포함하면 서로 다른 노드의 행은 PK 충돌이 없어 DB가 중복을 허용한다.

### 대응 방법
- 근본 해결: `roomId`에 유니크 제약을 걸거나 upsert(`MERGE`)를 쓰거나, 같은 트랜잭션에서 roomId 기준으로 락을 잡는다.
- 방어적 조회: 여러 건이 와도 예외를 던지지 말고 등록 순서가 가장 늦은 행을 고른다.

```java
Optional<String> findNode(String roomId) {
  return rows.stream()
      .filter(r -> r.roomId().equals(roomId))
      // 2건 이상이어도 예외 없이 가장 늦게 등록된 행을 택한다
      .max(Comparator.comparingLong(Row::seq))
      .map(Row::nodeId);
}
```

## 🔎 더 공부할 것

- MERGE / INSERT ... ON CONFLICT - 삭제 없이 한 문장으로 upsert
- SELECT ... FOR UPDATE - 행 락으로 동시 갱신 직렬화
- 낙관적 락(version 컬럼) - 충돌을 감지해 재시도하는 방식

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#concurrency` `#race-condition` `#unique-key` `#idempotent` | 2026-10-13 | [예제 보기](../../example/java/delete-then-insert-race) |
