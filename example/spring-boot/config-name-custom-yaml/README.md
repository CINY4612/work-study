# config-name-custom-yaml

`JDK 21 / Spring Boot / H2 in-memory` · 공통 프로젝트 `example/spring-boot` 의 의존성을 사용합니다.

## ▶ 실행

```bash
cd example/spring-boot
./gradlew :config-name-custom-yaml:bootRun
```

## ✅ 테스트

```bash
cd example/spring-boot
./gradlew :config-name-custom-yaml:test
```

## 📂 구조

```
src/main/java/dev/workhard/configname/ConfigNameApp.java
src/main/java/dev/workhard/configname/ShopInfo.java
src/main/resources/shop-settings.yaml
src/test/java/dev/workhard/configname/WithConfigNameTest.java
src/test/java/dev/workhard/configname/WithoutConfigNameTest.java
```

## 📝 관련 노트

- [application이 아닌 이름의 설정 파일 읽기](../../../studyNote/spring/2026-11-24-config-name-custom-yaml.md)
