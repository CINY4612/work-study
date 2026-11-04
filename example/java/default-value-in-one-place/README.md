# default-value-in-one-place

`JDK 21` · 공통 프로젝트 `example/java` 의 의존성을 사용합니다.

## ✅ 테스트

```bash
cd example/java
./gradlew :default-value-in-one-place:test
```

## 📂 구조

```
src/main/java/dev/workhard/OrderApi.java
src/main/java/dev/workhard/ShelfService.java
src/test/java/dev/workhard/ShelfServiceTest.java
```

## 📝 관련 노트

- [요청값 기본값은 서비스 한 곳에서만 정하기](../../../studyNote/java/2026-10-15-default-value-in-one-place.md)
