# left-join-zero-count-least-loaded

`JDK 21 / Spring Boot / H2 in-memory` · 공통 프로젝트 `example/spring-boot` 의 의존성을 사용합니다.

## ▶ 실행

```bash
cd example/spring-boot
./gradlew :left-join-zero-count-least-loaded:bootRun
```

## ✅ 테스트

```bash
cd example/spring-boot
./gradlew :left-join-zero-count-least-loaded:test
```

## 📂 구조

```
src/main/java/dev/workhard/App.java
src/main/java/dev/workhard/LoadRepository.java
src/test/java/dev/workhard/LoadRepositoryTest.java
```

## 📝 관련 노트

- [LEFT JOIN으로 개수가 0인 항목까지 집계하기](../../../studyNote/spring/2026-10-16-left-join-zero-count-least-loaded.md)
