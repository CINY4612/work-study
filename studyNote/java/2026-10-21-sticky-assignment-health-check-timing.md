<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [routing, health-check, sticky, assignment]
date: 2026-10-21
example: example/java/sticky-assignment-health-timing
status: auto
-->

# 배정 고정과 헬스 정보를 쓰는 시점 개념 정리

> [!NOTE]
> 헬스 정보는 새로 배정할 때만 참고하고, 배정이 끝난 작업은 노드 상태와 무관하게 같은 노드로 라우팅한다.

> [!WARNING]
> **헷갈린 점** · 헬스 정보로 배정 정보를 지우면 잠깐의 장애 판정에 진행 중인 작업이 끊기니, 삭제는 작업 종료 때만 한다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
작업(예: 채팅방, 세션)이 한 노드에 배정되면 그 작업은 노드 안에 상태(연결, 메모리)를 갖는다. 이를 **sticky 배정**이라 부른다. 이때 헬스체크(노드가 살아 있는지 주기적으로 확인하는 것) 결과로 배정 정보를 지우면, 헬스체크가 한 번 흔들렸을 뿐인데 멀쩡히 진행 중인 작업이 "없는 작업"이 된다.

### 동작 원리
1. 새 작업이 오면 살아 있는 노드만 후보로 거르고 부하가 가장 작은 노드를 고른다. (헬스 정보 사용)
2. 배정 결과를 `jobId -> nodeId`로 저장한다.
3. 이후 요청은 저장된 노드로 그대로 보낸다. (헬스 정보 사용 안 함)
4. 작업이 끝나거나 연결 오류로 종료될 때만 배정 정보를 지운다.

```text
신규 요청 -> [헬스 확인] -> 노드 선택 -> 배정 저장
후속 요청 -> 배정 조회 -> 같은 노드로 전달 (헬스 무시)
종료/오류 -> 배정 삭제
```

### 구성 요소/용어 정리
| 용어 | 뜻 |
| --- | --- |
| 배정 정보 | 작업 id와 노드의 매핑 |
| 헬스 정보 | 노드가 정상인지 기록한 값 |
| sticky | 한 번 정해진 노드를 계속 쓰는 성질 |
| 정리 시점 | 배정 정보를 지우는 유일한 순간 (종료) |

### 코드로 보기
```java
public String assign(String jobId) {
    String existing = assignment.get(jobId);
    if (existing != null) return existing;      // 이미 배정됨: 헬스 정보를 보지 않는다
    String best = load.keySet().stream()
        .filter(n -> !down.contains(n))          // 신규 배정에서만 장애 노드를 제외
        .min(Comparator.comparingInt(load::get)) // 부하가 가장 작은 노드
        .orElseThrow(() -> new IllegalStateException("no alive node"));
    assignment.put(jobId, best);
    load.merge(best, 1, Integer::sum);
    return best;
}
```

### 비교
| 방식 | 장점 | 단점 |
| --- | --- | --- |
| 라우팅 때마다 헬스 확인 후 삭제 | 죽은 노드로 안 보냄 | 일시적 장애에도 정상 작업이 끊김, 복구 후에도 복원 불가 |
| 배정 후 헬스 무시 (이 노트) | 진행 중인 작업 유지 | 진짜 죽은 노드로 요청이 가면 실패하므로 종료 경로가 정리해 줘야 함 |

### 자주 하는 실수와 주의점
- 조회 메서드(`get`) 안에서 정리까지 하면 읽기 호출이 데이터를 지우는 부작용이 된다. 조회는 읽기만 하게 둔다.
- 종료 경로(정상 종료, 오류 종료)가 하나라도 빠지면 배정 정보가 남아 부하 수가 실제보다 크게 잡힌다.
- 진짜 장애인 노드로 가는 요청은 실패하거나 timeout이 날 수 있다. 이 비용을 받아들이는 설계 판단이다.

### 세션에서 나온 질문 정리
- Q. 배정 후 헬스체크 삭제 로직을 빼면 어떻게 되나?
A. 진행 중인 작업이 유지된다. 대신 죽은 노드의 배정 정보는 종료 경로가 지울 때까지 남는다.
- Q. 그럼 장애 노드의 대기 중인 사용자는?
A. 진행 중인 작업과 별개로, 대기 상태일 때만 다른 노드로 옮기는 알림을 보내 이동시킨다.
- Q. 어디서 지워야 하나?
A. 작업이 끝나는 곳(종료, 오류 종료)에만 둔다.

## 🔎 더 공부할 것

- Sticky session - 로드밸런서의 세션 고정과 비교
- Circuit breaker - 장애 노드로 가는 요청 차단
- Graceful drain - 노드 교체 시 진행 중인 작업 마무리

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#routing` `#health-check` `#sticky` `#assignment` | 2026-10-21 | [예제 보기](../../example/java/sticky-assignment-health-timing) |
