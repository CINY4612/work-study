<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [spring-boot, externalized-config, spring.config.name, yaml]
date: 2026-11-24
example: example/spring-boot/config-name-custom-yaml
status: auto
-->

# application이 아닌 이름의 설정 파일 읽기

> [!NOTE]
> Spring Boot는 기본으로 application.yaml/properties만 자동 로드하고, 다른 이름의 파일은 spring.config.name을 지정해야 읽는다.

> [!WARNING]
> **헷갈린 점** · 설정이 통째로 안 읽혀도 첫 에러는 값 하나가 없다는 식으로 보여서, 파일 자체를 못 읽은 건지 알아채기 어렵다.

## 📖 자세히

### 왜 이렇게 동작할까
Spring Boot는 기동할 때 classpath와 현재 디렉터리에서 `application.*` 이름의 파일만 찾습니다. 이 기본 이름을 "config name"이라고 하고, `spring.config.name`으로 바꿀 수 있습니다. 파일 이름만 다르게 두면 에러 없이 무시되어, 그 파일의 값이 전부 빠진 채 실행됩니다.

1. `spring.config.name=shop-settings` 지정: `shop-settings.yaml`을 기본 위치(classpath 루트 등)에서 찾음
2. `spring.config.location=file:/경로/파일.yaml` 지정: 이름이 아니라 정확한 경로를 지정하며, 이 경우 이름 규칙과 상관없이 읽음
3. 둘 다 없으면 `application.*`만 읽음

운영에서는 `--spring.config.location`으로 넘기고 로컬 IDE에서는 인자를 빼먹어서, 로컬에서만 설정이 비는 일이 흔합니다.

### 코드로 보기
```java
// 설정 파일: src/main/resources/shop-settings.yaml  (shop.name: 책방)
@Component
class ShopInfo {
    // 값이 없을 때 기본값을 두면 "파일을 못 읽음"이 조용히 숨겨진다
    @Value("${shop.name:미설정}")
    String name;
}

// 테스트: config name을 지정해야 shop-settings.yaml을 읽는다
@SpringBootTest(properties = "spring.config.name=shop-settings")
class WithConfigNameTest { /* name == "책방" */ }
```

### 자주 하는 실수
- 파일 이름만 바꿔 두고 지정을 빼먹으면 에러가 아니라 "값 없음"으로 나타납니다. 첫 실패 항목만 보지 말고 파일이 로드됐는지부터 확인합니다.
- 코드에서 `System.setProperty("spring.config.name", ...)`로 기본값을 줄 수도 있지만, 이름을 바꾸기 어려우면 파일명을 `application.yaml`로 두는 편이 가장 단순합니다.

## 🔎 더 공부할 것

- spring.config.location - 이름이 아니라 경로로 지정하고, 여러 위치를 쓰는 방법
- spring.config.import - 기본 설정 파일에서 추가 파일을 불러오기
- https://docs.spring.io/spring-boot/reference/features/external-config.html - Externalized Configuration 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#spring-boot` `#externalized-config` `#spring.config.name` `#yaml` | 2026-11-24 | [예제 보기](../../example/spring-boot/config-name-custom-yaml) |
