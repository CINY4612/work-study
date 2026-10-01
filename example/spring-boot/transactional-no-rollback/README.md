# transactional-no-rollback

`JDK 21 / Spring Boot / Gradle Wrapper / H2 in-memory`

## ▶ 실행

```bash
./gradlew bootRun
```

## ✅ 테스트

```bash
./gradlew test
```

## 📂 구조

```
src/main/java/dev/workhard/App.java
src/main/java/dev/workhard/AssignmentService.java
src/test/java/dev/workhard/AssignmentServiceTest.java
```

## 📝 관련 노트

- [예외를 던져도 정리 작업은 커밋하고 싶을 때 noRollbackFor를 쓰는 법](../../../studyNote/spring/2026-10-01-transactional-no-rollback-for.md)
