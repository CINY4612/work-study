<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [routing, state-machine, draining, dedupe-alert]
date: 2026-10-28
example: example/java/unavailable-worker-routing
status: auto
-->

# 휴가 중인 작업자를 건너뛰는 라우팅 개념 정리

> [!NOTE]
> 작업자를 '휴가 예정(LEAVING)'을 거쳐 '휴가 중(AWAY)'으로 바꾸고, 새 요청은 담당자 대신 조정자에게 되돌리는 패턴이다.

> [!WARNING]
> **헷갈린 점** · 되돌림 횟수에 한도를 두고, 한도를 넘으면 알림은 한 번만 보내 알림 폭주를 막는다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
작업을 여러 담당자에게 나눠 주는 구조에서 담당자 한 명이 자리를 비울 때, 그 사람을 빼고 흐름이 계속 돌게 하는 방법이다. 단순히 상태 값만 바꾸면 두 가지 문제가 생긴다.
- 이미 받은 일이 있는데 바로 '부재'로 바꾸면 그 일이 갈 곳이 없어진다.
- 부재자에게 요청이 계속 오면 되돌림이 무한히 반복되거나, 알림이 매번 나가 시끄러워진다.

### 동작 원리
1. 휴가를 보내면 처리 중인 일이 있는지 본다. 있으면 `LEAVING`(새 요청은 막고 기존 일은 마무리하는 중), 없으면 바로 `AWAY`가 된다. 이렇게 진행 중인 일을 다 비운 뒤 멈추는 방식을 draining(배수)이라고 한다.
2. 새 요청이 부재자(LEAVING/AWAY)에게 가면 조정자에게 되돌린다. 조정자가 다음 단계 담당자에게 다시 배정한다.
3. 되돌림 횟수를 센다. 한도(`BOUNCE_LIMIT`)를 넘는 순간 한 번만 알림을 남기고, 이후는 조용히 되돌린다.
4. 마지막 일을 끝내면 `LEAVING`이 `AWAY`로 바뀐다.
5. 복귀시키면 `ACTIVE`로 돌리고 되돌림 횟수를 초기화한다. 다음 휴가는 새 상황이라 다시 한 번 알릴 수 있어야 하기 때문이다.

```text
ACTIVE --휴가(일 있음)--> LEAVING --마지막 일 완료--> AWAY
ACTIVE --휴가(일 없음)--------------------------> AWAY
LEAVING/AWAY --복귀--> ACTIVE
```

### 구성 요소/용어 정리
| 용어 | 뜻 |
|---|---|
| 조정자(coordinator) | 요청을 받아 담당자에게 나눠 주는 역할. 휴가 불가 |
| LEAVING | 새 일은 받지 않고 기존 일만 마무리하는 중간 상태 |
| AWAY | 완전히 빠진 상태 |
| 되돌림(bounce) | 부재자에게 간 요청을 조정자에게 돌려보내는 것 |

### 코드로 보기
```java
// 배정: 가용하면 본인에게, 아니면 조정자에게 되돌린다
public String assign(String worker) {
    if (status(worker) == Status.ACTIVE) {
        pending.merge(worker, 1, Integer::sum);
        return worker;
    }
    int n = bounces.merge(worker, 1, Integer::sum);
    if (n == BOUNCE_LIMIT + 1) {          // 한도를 처음 넘는 순간에만 알림
        alerts.add(worker + " 앞 요청이 반복해서 되돌려짐");
    }
    return coordinator;
}
```
```java
// 일 완료: 마지막 일이 끝나면 LEAVING -> AWAY
public void finish(String worker) {
    int left = pending.merge(worker, -1, Integer::sum);
    if (left == 0 && status(worker) == Status.LEAVING) {
        status.put(worker, Status.AWAY);
    }
}
```

### 비교
| 방식 | 장점 | 단점 |
|---|---|---|
| 상태만 AWAY로 즉시 변경 | 단순 | 진행 중인 일이 갈 곳을 잃음 |
| LEAVING을 거침 | 일을 안전하게 마무리 | 상태가 하나 늘어남 |
| 되돌림마다 알림 | 놓치지 않음 | 알림 폭주 |
| 한도 초과 시 1회 알림 | 조용하고 충분함 | 횟수 저장이 필요 |

### 자주 하는 실수와 주의점
- 조정자까지 휴가를 허용하면 되돌릴 곳이 없어진다. 서버(도메인 규칙)에서 거부해야 하며 화면 버튼 비활성화만으로는 부족하다.
- 알림 여부를 메모리 플래그로만 두면 재시작 때 다시 울린다. 실제 서비스에서는 횟수나 알림 기록을 상태와 함께 저장한다.
- 막는 지점을 한 곳에만 두면 새 경로가 생길 때 구멍이 난다. 프롬프트/배정/최종 전달처럼 여러 층에서 확인하는 것이 안전하다.

### 세션에서 나온 질문 정리
- Q. 일하는 중인 사람을 휴가 보내면?
  A. 즉시 빠지지 않고 LEAVING으로 두었다가 마지막 일이 끝나면 AWAY가 된다.
- Q. 되돌림이 계속되면?
  A. 한도를 넘는 순간 1회만 알리고 이후는 조용히 되돌린다.
- Q. 복귀할 때 되돌림 횟수는?
  A. 초기화한다. 다음 휴가에서 다시 1회 알릴 수 있다.

## 🔎 더 공부할 것

- 상태 머신(enum + 전이 규칙) - 허용되지 않은 전이를 막는 설계
- Draining / graceful shutdown - 진행 중인 일을 마치고 멈추는 방식
- 알림 중복 제거(dedupe) - 같은 이벤트를 한 번만 알리는 기준

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#routing` `#state-machine` `#draining` `#dedupe-alert` | 2026-10-28 | [예제 보기](../../example/java/unavailable-worker-routing) |
