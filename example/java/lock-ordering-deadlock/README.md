# lock-ordering-deadlock

`JDK 21` · 공통 프로젝트 `example/java` 의 의존성을 사용합니다.

## ✅ 테스트

```bash
cd example/java
./gradlew :lock-ordering-deadlock:test
```

## 📂 구조

```
src/main/java/dev/workhard/Account.java
src/main/java/dev/workhard/Bank.java
src/test/java/dev/workhard/BankTest.java
```

## 📝 관련 노트

- [락 획득 순서를 고정해서 데드락을 피하는 법](../../../studyNote/java/2026-10-02-lock-ordering-deadlock.md)
