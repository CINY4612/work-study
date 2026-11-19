# inject-save-path-for-tests

`JDK 21` · 공통 프로젝트 `example/java` 의 의존성을 사용합니다.

## ✅ 테스트

```bash
cd example/java
./gradlew :inject-save-path-for-tests:test
```

## 📂 구조

```
src/main/java/dev/workhard/savepath/SaveStore.java
src/main/java/dev/workhard/savepath/Shop.java
test/java/dev/workhard/savepath/ShopSaveTest.java
```

## 📝 관련 노트

- [테스트가 실제 저장 파일을 덮어쓰지 않게 하기](../../../studyNote/java/2026-11-19-inject-save-path-for-tests.md)
