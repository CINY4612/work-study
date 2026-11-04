# after-commit-notification

`JDK 21 / Spring Boot / H2 in-memory` · 공통 프로젝트 `example/spring-boot` 의 의존성을 사용합니다.

## ▶ 실행

```bash
cd example/spring-boot
./gradlew :after-commit-notification:bootRun
```

## ✅ 테스트

```bash
cd example/spring-boot
./gradlew :after-commit-notification:test
```

## 📂 구조

```
src/main/java/dev/workhard/App.java
src/main/java/dev/workhard/OrderService.java
src/test/java/dev/workhard/OrderServiceTest.java
```

## 📝 관련 노트

- [트랜잭션이 커밋된 뒤에만 외부 알림을 보내는 법](../../../studyNote/spring/2026-10-01-after-commit-notification.md)
