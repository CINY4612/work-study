<!-- meta (read by scripts, hidden on GitHub)
category: typescript
tags: [idempotency, race-condition, guard, event]
date: 2026-10-07
example: example/node-express/idempotent-join-guard
status: auto
-->

# 비동기로 바뀌는 상태 대신 현재 처리 중인 ID로 중복 호출을 막는 법

> [!NOTE]
> 같은 룸에 소켓 이벤트와 HTTP 응답이 각각 join을 부르면, 서버 응답으로 바뀌는 status 검사는 두 번째 호출을 못 막는다.

> [!WARNING]
> **헷갈린 점** · 호출 시점에 동기적으로 기록하는 activeRoomId 가드를 쓰면 먼저 온 쪽 하나만 실행되고 순서는 신경 쓰지 않아도 된다.

## 📖 자세히

### 왜 status 검사로는 안 막힐까
한 동작에 호출 경로가 둘이다. (A) 서버가 보내는 소켓 이벤트 핸들러, (B) 요청의 HTTP 응답 콜백. 둘은 서로 다른 비동기 채널이라 도착 순서가 보장되지 않고, 둘 다 실행될 수 있다.

기존 가드가 `status !== 'READY'`이면 건너뛰는 방식이라면, status는 **서버 응답을 받아야** 바뀐다. 첫 join을 호출한 직후에도 status는 아직 READY라서 두 번째 호출이 그대로 통과한다. 즉 "가드가 보는 값"이 "가드를 통과한 사실"보다 늦게 갱신되는 것이 문제다.

### 해결: 통과하는 순간 동기적으로 기록한다
JS는 한 번에 한 콜백만 실행하므로, 검사와 기록을 같은 동기 구간에 두면 둘 사이에 다른 호출이 끼어들 수 없다.

```ts
// 나쁜 예: 비동기로 갱신되는 status에 의존
if (this.status !== 'READY') return;
this.doJoin(roomId);            // status는 아직 READY -> 두 번째도 통과

// 좋은 예: 통과하는 즉시 기록
if (this.activeRoomId === roomId) return false;
this.activeRoomId = roomId;     // 같은 동기 구간에서 기록
this.doJoin(roomId);
// 룸을 떠나거나 초기화할 때 반드시 null로 되돌린다
```

주의: 값을 되돌리는 지점을 빠뜨리면 이후 정상 재입장까지 막힌다. 실패 시에도 해제해야 한다.

## 🔎 더 공부할 것

- 멱등성(idempotency) - 여러 번 호출해도 결과가 같게 만드는 설계
- Promise 단위 중복 제거 - 진행 중인 Promise를 맵에 저장해 재사용
- 이벤트 루프 - 왜 동기 구간에서는 끼어들기가 없는지 https://developer.mozilla.org/docs/Web/JavaScript/Event_loop

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `typescript` | `#idempotency` `#race-condition` `#guard` `#event` | 2026-10-07 | [▶ 실행해보기](../../example/node-express/idempotent-join-guard) |
