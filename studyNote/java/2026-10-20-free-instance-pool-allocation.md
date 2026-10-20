<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [allocation, pool, heartbeat, fifo, idempotent]
date: 2026-10-20
example: example/java/free-instance-pool-allocation
status: auto
-->

# 빈 인스턴스 풀 배정 개념 정리

> [!NOTE]
> 빈 인스턴스 목록에서 살아 있는 것 중 가장 오래 비어 있던 것을 꺼내 룸에 붙이고, 같은 룸이 다시 오면 같은 인스턴스를 돌려준다.

> [!WARNING]
> **헷갈린 점** · 부하를 재지 않는 FIFO 방식이라 부하 분산이 아니라 순환 사용이 목적이다. 재접속 때 같은 인스턴스가 필요해서 룸 기준 고정이 들어간다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
미리 띄워 둔 인스턴스(예: 화상 처리 컨테이너)를 요청(룸)마다 하나씩 빌려주는 방식이다. 풀(pool)은 재사용할 자원을 모아 둔 목록이다. 인스턴스마다 한 번에 한 룸만 쓸 수 있어서, 누가 어떤 인스턴스를 쓰는지 한 곳에서 기록해야 한다.

### 동작 원리
1. 요청이 오면 먼저 그 룸에 이미 배정된 인스턴스가 있는지 본다. 있으면 상태와 관계없이 그대로 돌려준다. (재접속해도 같은 인스턴스에 붙어야 하기 때문이다.)
2. 없으면 빈 목록을 `비어진 시각` 오름차순으로 훑는다.
3. 살아 있는 첫 인스턴스를 고른다. 죽은 인스턴스는 건너뛴다.
4. 고른 인스턴스를 빈 목록에서 빼고 `룸 -> 인스턴스`로 기록한다.
5. 고를 것이 없으면 빈 결과(배정 실패)를 돌려준다.
6. 룸이 끝나면 해제한다. 인스턴스는 `해제 시각`을 점수로 다시 빈 목록 맨 뒤에 들어간다.

```text
acquire(room) -> [룸에 이미 배정?] -yes-> 그 인스턴스 반환
                       |no
                       v
   빈 목록(오래 빈 순) -> 살아 있는 첫 후보 -> busy로 이동 -> 반환
                       |없음
                       v
                    배정 실패
```

### 구성 요소/용어 정리
| 용어 | 뜻 |
| --- | --- |
| 빈 목록(free) | 지금 쓸 수 있는 인스턴스와 비어진 시각(score) |
| 룸 매핑 | 룸 id -> 배정된 인스턴스 id |
| 생존 여부 | heartbeat(주기적으로 남기는 생존 신호)가 만료되지 않았는지 |
| score | 정렬 기준 값. 여기서는 해제된 시각 |

### 코드로 보기
```java
public Optional<String> allocate(String roomId) {
    String existing = roomToInstance.get(roomId);
    if (existing != null) return Optional.of(existing);   // 재요청은 같은 인스턴스

    Optional<String> pick = freeSince.entrySet().stream()
        .filter(e -> alive.contains(e.getKey()))           // 죽은 것은 건너뜀
        .min(Map.Entry.<String, Long>comparingByValue()    // 가장 오래 빈 것
            .thenComparing(Map.Entry.comparingByKey()))    // 같은 시각이면 id 순
        .map(Map.Entry::getKey);

    pick.ifPresent(id -> {
        freeSince.remove(id);                              // 빈 목록에서 제거
        roomToInstance.put(roomId, id);                    // 룸에 고정
    });
    return pick;
}
```

### 비교
| 방식 | 고르는 기준 | 특징 |
| --- | --- | --- |
| 가장 오래 빈 것(FIFO) | 해제된 시각 | 구현이 단순하고 골고루 쓰이지만 부하는 모른다 |
| 최소 부하 | 현재 처리 건수 | 부하는 맞추지만 건수를 따로 관리해야 한다 |
| 라운드 로빈 | 순번 | 상태가 필요 없지만 죽은 대상을 직접 건너뛰어야 한다 |

### 자주 하는 실수와 주의점
- 룸 기준 조회를 먼저 하지 않으면 재접속할 때 새 인스턴스가 배정되어 기존 세션을 잃는다.
- 배정 도중 다른 요청이 끼어들면 같은 인스턴스가 두 번 나간다. 실제 저장소(예: Redis)에서는 조회와 갱신을 한 스크립트로 묶어 원자적으로 처리해야 한다. 이 예제는 단일 스레드 기준이다.
- 살아 있는지는 목록에 있는지가 아니라 heartbeat로 판단해야 한다. 죽은 인스턴스가 빈 목록에 남아 있을 수 있다.

### 세션에서 나온 질문 정리
**Q. 배정 대상은 서버(agent)인가, 인스턴스인가?**
A. 인스턴스다. 서버 하나에 인스턴스가 여러 개 있고, 인스턴스 id를 서버 id와 포트로 구성해 어느 서버 소속인지 알 수 있게 한다.

**Q. 서버별 부하는 왜 안 따지나?**
A. 이 방식은 가장 오래 쉰 인스턴스를 고를 뿐이라 서버별 부하는 고려하지 않는다. 부하를 맞추려면 최소 부하 선택을 따로 넣어야 한다.

**Q. 구역(센터)별로 나누려면?**
A. 풀에 구역 정보가 없으면 어느 구역 인스턴스든 먼저 비었던 것이 나간다. 구역마다 풀을 따로 두거나, 요청에 구역을 실어 후보를 거르면 된다.

## 🔎 더 공부할 것

- Redis Lua 스크립트 - 조회와 갱신을 원자적으로 묶는 방법
- heartbeat TTL - 생존 신호의 만료로 죽은 대상을 판단하는 방식
- 최소 부하 선택 - FIFO 대신 부하를 기준으로 고르는 방식

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#allocation` `#pool` `#heartbeat` `#fifo` `#idempotent` | 2026-10-20 | [예제 보기](../../example/java/free-instance-pool-allocation) |
