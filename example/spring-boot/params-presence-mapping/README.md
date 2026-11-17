# params-presence-mapping

`JDK 21 / Spring Boot / H2 in-memory` · 공통 프로젝트 `example/spring-boot` 의 의존성을 사용합니다.

## ✅ 테스트

```bash
cd example/spring-boot
./gradlew :params-presence-mapping:test
```

## 📂 구조

```
src/main/java/dev/workhard/EntryController.java
src/main/java/dev/workhard/ErrorPageController.java
src/test/java/dev/workhard/ParamsMappingTest.java
```

## 📝 관련 노트

- [@GetMapping params로 쿼리 키 존재만 매칭하기](../../../studyNote/spring/2026-10-22-params-presence-mapping.md)
