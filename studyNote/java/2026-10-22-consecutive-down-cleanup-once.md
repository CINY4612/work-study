<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [health-check, state, counter, reset]
date: 2026-10-22
example: example/java/consecutive-down-cleanup-once
status: auto
-->

# 연속 장애일 때 한 번만 정리하고 복구 시 초기화

> [!NOTE]
> 장애가 N번 연속일 때 정리를 한 번만 실행하려면 연속 횟수와 정리 완료 플래그를 함께 관리하고, 정상이면 둘 다 초기화한다.

> [!WARNING]
> **헷갈린 점** · 조건 판단을 메서드 밖으로 빼다가 정상일 때의 초기화(else)가 빠지기 쉽다. 그러면 띄엄띄엄 난 장애도 누적된다.

## 📖 자세히

### 왜 초기화가 필요할까
주기적으로 상태를 확인하다가 "연속 N번 DOWN"일 때 정리 작업(예: 해당 노드의 배정 정보 삭제)을 하고 싶다고 하자. 잠깐의 장애에 정리가 실행되면 안 되므로 "연속"이 핵심이다.

1. DOWN이면 `downCycles`를 1 올린다.
2. `downCycles >= threshold`이고 아직 정리하지 않았다면 정리하고 `cleaned = true`로 둔다. 장애가 이어지는 동안 반복 삭제하지 않는다.
3. 정상이면 `downCycles = 0`, `cleaned = false`로 되돌린다. 그래야 "연속"이 유지되고, 복구 후 다시 죽으면 또 정리한다.
4. 정리가 실패하면 `cleaned`를 되돌려 다음 주기에 재시도한다.

```java
// 잘못된 호출 구조: DOWN일 때만 호출하면 정상일 때의 초기화 코드가 실행되지 않는다
if (down) { trigger.onCycle(true); }

// 올바른 구조: 정상 주기도 반드시 전달해 초기화되게 한다
if (trigger.onCycle(down)) {
    try { cleanup(); } catch (Exception e) { trigger.cleanupFailed(); }
}
```

호출 조건을 읽기 쉽게 밖으로 빼는 리팩터링에서 이 else가 사라지는 실수가 자주 나온다. 테스트가 "중간에 복구되면 다시 센다", "복구 후 다시 죽으면 다시 정리한다"를 확인해야 잡힌다.

## 🔎 더 공부할 것

- 상태 머신 - 상태 전이로 연속 장애를 표현하기
- 멱등성(idempotent) - 정리 작업을 여러 번 실행해도 안전하게 만들기
- 지수 백오프 - 장애가 이어질 때 확인 간격 늘리기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#health-check` `#state` `#counter` `#reset` | 2026-10-22 | [예제 보기](../../example/java/consecutive-down-cleanup-once) |
