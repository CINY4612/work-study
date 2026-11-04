# inner-join-drops-rows-without-child

`JDK 21 / Spring Boot / H2 in-memory` · 공통 프로젝트 `example/spring-boot` 의 의존성을 사용합니다.

## ▶ 실행

```bash
cd example/spring-boot
./gradlew :inner-join-drops-rows-without-child:bootRun
```

## ✅ 테스트

```bash
cd example/spring-boot
./gradlew :inner-join-drops-rows-without-child:test
```

## 📂 구조

```
src/main/java/dev/workhard/App.java
src/main/java/dev/workhard/MemberQuery.java
src/test/java/dev/workhard/MemberQueryTest.java
```

## 📝 관련 노트

- [INNER JOIN이 설정 행 없는 계정을 지우는 문제](../../../studyNote/spring/2026-11-03-inner-join-drops-rows-without-child.md)
