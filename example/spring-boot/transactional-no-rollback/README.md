# transactional-no-rollback

`JDK 21 / Spring Boot / H2 in-memory` · 공통 프로젝트 `example/spring-boot` 의 의존성을 사용합니다.

## ▶ 실행

```bash
cd example/spring-boot
./gradlew :transactional-no-rollback:bootRun
```

## ✅ 테스트

```bash
cd example/spring-boot
./gradlew :transactional-no-rollback:test
```

## 📂 구조

```
src/main/java/dev/workhard/App.java
src/main/java/dev/workhard/AssignmentService.java
src/test/java/dev/workhard/AssignmentServiceTest.java
```

## 📝 관련 노트

- [예외를 던져도 커밋되게 하는 noRollbackFor](../../../studyNote/spring/2026-10-01-transactional-no-rollback-for.md)
