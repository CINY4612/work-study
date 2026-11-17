# replication-lag-and-failover-basics

`JDK 21` · 공통 프로젝트 `example/java` 의 의존성을 사용합니다.

## ✅ 테스트

```bash
cd example/java
./gradlew :replication-lag-and-failover-basics:test
```

## 📂 구조

```
src/main/java/dev/workhard/replication/Quorum.java
src/main/java/dev/workhard/replication/ReplicatedStore.java
src/test/java/dev/workhard/replication/ReplicationTest.java
```

## 📝 관련 노트

- [Primary-Replica 복제와 장애 전환 개념 정리](../../../studyNote/java/2026-11-17-replication-lag-and-failover-basics.md)
