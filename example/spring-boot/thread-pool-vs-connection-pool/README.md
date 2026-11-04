# thread-pool-vs-connection-pool

`JDK 21 / Spring Boot / H2 in-memory` · 공통 프로젝트 `example/spring-boot` 의 의존성을 사용합니다.

## ✅ 테스트

```bash
cd example/spring-boot
./gradlew :thread-pool-vs-connection-pool:test
```

## 📂 구조

```
src/main/java/dev/workhard/pool/PoolDemo.java
src/test/java/dev/workhard/pool/PoolDemoTest.java
```

## 📝 관련 노트

- [톰캣 스레드를 늘려도 DB 커넥션 풀이 병목이면 소용없다](../../../studyNote/spring/2026-10-08-thread-pool-vs-connection-pool.md)
