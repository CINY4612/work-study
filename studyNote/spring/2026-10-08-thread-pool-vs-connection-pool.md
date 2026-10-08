<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [hikaricp, tomcat, transactional, connection-pool]
date: 2026-10-08
example: example/spring-boot/thread-pool-vs-connection-pool
status: auto
-->

# 톰캣 스레드를 늘려도 DB 커넥션 풀이 병목이면 소용없다

> [!NOTE]
> 요청 스레드를 늘려도 처리량은 그 뒤의 가장 좁은 풀(DB 커넥션 등)이 정한다. 늘린 만큼 커넥션을 기다리는 스레드만 늘어난다.

> [!WARNING]
> **헷갈린 점** · @Transactional 안에서 느린 외부 호출을 하면 그동안 커넥션을 잡고 있어 풀이 더 빨리 마른다. 느린 호출은 트랜잭션 밖으로 빼자.

## 📖 자세히

### 왜 이렇게 동작할까

웹 요청은 여러 풀을 차례로 지난다. 톰캣 요청 스레드(기본 최대 200개) -> 서비스 로직 -> HikariCP 커넥션 풀(기본 최대 10개) -> DB. 처리량은 가장 좁은 구간이 정한다. 스레드가 200개라도 커넥션이 10개면 DB를 쓰는 요청은 동시에 10개만 진행되고 나머지 190개는 줄을 선다. 대기 시간이 `connectionTimeout`(기본 30초)을 넘으면 `SQLTransientConnectionException`이 난다.

`@Transactional` 메서드는 시작할 때 커넥션을 빌려 메서드가 끝날 때(커밋/롤백) 반납한다. 그래서 메서드 안에서 느린 HTTP 호출이나 상태 확인을 기다리면, DB 일은 하지 않으면서 커넥션만 점유하게 된다. 점유 시간이 길수록 풀 10개는 금방 바닥난다.

### 나쁜 예 vs 좋은 예

```java
// 나쁜 예: 느린 호출을 하는 동안 커넥션을 계속 쥐고 있다
try (Connection c = ds.getConnection()) {
    slowCall(ms);              // 커넥션을 쥔 채 대기
    c.createStatement().execute("select 1");
}

// 좋은 예: 느린 호출을 먼저 끝내고, 필요한 순간에만 커넥션을 빌린다
slowCall(ms);
try (Connection c = ds.getConnection()) {
    c.createStatement().execute("select 1");
}
```

### 정리
- 스레드 수를 올리기 전에 어느 풀에서 대기가 생기는지 먼저 본다 (`HikariPoolMXBean`의 active/waiting 수).
- 커넥션 풀을 키우면 DB 세션 수가 늘고, 서버 인스턴스가 N대면 N배가 되므로 DB 쪽 한도도 함께 봐야 한다.
- 트랜잭션 범위를 DB 작업만 감싸도록 줄이는 것이 가장 싼 해결책이다.

## 🔎 더 공부할 것

- HikariCP maximumPoolSize / connectionTimeout - 풀 크기를 정하는 기준과 대기 타임아웃
- 트랜잭션 경계 줄이기 - 외부 호출과 DB 작업을 분리하는 설계
- HikariPoolMXBean - 활성/대기 커넥션 수를 관측하는 방법
- Tomcat server.tomcat.threads.max - 요청 스레드 풀 설정의 의미

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#hikaricp` `#tomcat` `#transactional` `#connection-pool` | 2026-10-08 | [예제 보기](../../example/spring-boot/thread-pool-vs-connection-pool) |
