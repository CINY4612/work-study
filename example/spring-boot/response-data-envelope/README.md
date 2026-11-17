# response-data-envelope

`JDK 21 / Spring Boot / H2 in-memory` · 공통 프로젝트 `example/spring-boot` 의 의존성을 사용합니다.

## ▶ 실행

```bash
cd example/spring-boot
./gradlew :response-data-envelope:bootRun
```

## ✅ 테스트

```bash
cd example/spring-boot
./gradlew :response-data-envelope:test
```

## 📂 구조

```
src/main/java/dev/workhard/envelope/Envelope.java
src/main/java/dev/workhard/envelope/EnvelopeApplication.java
src/main/java/dev/workhard/envelope/FlatResponse.java
src/test/java/dev/workhard/envelope/EnvelopeTest.java
```

## 📝 관련 노트

- [응답 부가 데이터는 data 안에 담기](../../../studyNote/spring/2026-10-16-response-data-envelope.md)
