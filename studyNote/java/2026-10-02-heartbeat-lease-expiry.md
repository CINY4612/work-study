<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [heartbeat, lease, ttl, clock, concurrency]
date: 2026-10-02
example: example/java/heartbeat-lease-expiry
status: auto
-->

# 만료 시각과 주입한 Clock으로 heartbeat 생존 판정

> [!NOTE]
> heartbeat마다 '만료 시각'을 갱신해 두고 현재 시각과 비교하면 Redis TTL 없이도 생존 여부와 자리 선점을 판정할 수 있다.

> [!WARNING]
> **헷갈린 점** · 선점은 '비어 있거나 만료됐을 때만' 한 번에 처리해야 한다. 조회 후 저장으로 나누면 두 인스턴스가 동시에 이긴다.

## 📖 자세히

### 왜 이렇게 동작할까

Redis의 `EXPIRE`(키 TTL)는 저장소가 시간을 직접 관리해 주기 때문에, 키가 있으면 살아 있다고 볼 수 있다. DB나 메모리 맵으로 옮기면 그 기능이 없으므로 **만료 시각(expiresAt)을 값으로 저장하고 읽을 때 현재 시각과 비교**하는 방식으로 바꿔야 한다. 이를 lease(임대) 패턴이라고 부른다. 일정 기간 자리를 빌려 주고, heartbeat(주기적 생존 신호)로 기간을 연장한다.

1. heartbeat: `expiresAt = now + ttl` 로 덮어쓴다.
2. 생존 판정: `now < expiresAt` 이면 살아 있다.
3. 선점(acquire): 비어 있거나 `now >= expiresAt` 일 때만 성공해야 하며, 이 판단과 저장이 한 덩어리로 실행되어야 한다. Redis에서는 Lua 스크립트가 맡던 역할이고, 자바에서는 `ConcurrentHashMap.compute`, DB에서는 조건부 `UPDATE ... WHERE expires_at <= :now` 가 같은 역할을 한다.
4. 시간은 `Clock`을 주입받아 쓴다. `Instant.now()`를 직접 부르면 TTL 테스트를 하려고 실제로 sleep 해야 한다.

```java
// 판단과 저장을 compute 한 번에 묶어 원자적으로 만든다
public boolean tryAcquire(String id) {
    Instant now = clock.instant();
    AtomicBoolean won = new AtomicBoolean(false);
    expiresAt.compute(id, (k, old) -> {
        if (old == null || !now.isBefore(old)) { // 비었거나 만료됨
            won.set(true);
            return now.plus(ttl);
        }
        return old; // 아직 유효하면 그대로 둔다
    });
    return won.get();
}
```

### 잘못된 예
`if (!isAlive(id)) { expiresAt.put(id, ...); }` 는 확인과 저장 사이에 다른 스레드가 끼어들 수 있다(check-then-act 경합). 둘 다 성공으로 판단하게 된다.

### 주의
여러 서버가 각자 시계를 쓰면 시계 오차가 판정에 들어간다. DB를 쓴다면 앱 시각 대신 DB 시각을 기준으로 삼는 편이 안전하다.

## 🔎 더 공부할 것

- ConcurrentHashMap.compute - 키 단위 원자 갱신이 보장되는 범위
- 조건부 UPDATE(낙관적 갱신) - DB에서 Lua 원자성을 대신하는 방법
- java.time.Clock - 테스트용 시계 주입 방식 https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html
- 분산 락과 fencing token - 만료된 소유자가 뒤늦게 쓰는 문제

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#heartbeat` `#lease` `#ttl` `#clock` `#concurrency` | 2026-10-02 | [예제 보기](../../example/java/heartbeat-lease-expiry) |
