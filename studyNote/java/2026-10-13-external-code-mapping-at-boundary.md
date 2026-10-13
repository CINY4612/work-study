<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [mapping, boundary, design, fallback]
date: 2026-10-13
example: example/java/external-code-mapping-at-boundary
status: auto
-->

# 외부 코드는 경계에서 내부 코드로 한 번만 변환하기

> [!NOTE]
> 외부 시스템이 보내는 코드는 입구에서 내부 코드로 한 번만 바꾸고, 안쪽 로직은 내부 코드만 비교하게 한다.

> [!WARNING]
> **헷갈린 점** · 매핑에 없는 값을 그대로 통과시킬지(lenient), 실패시킬지(strict)는 장애 때 어떻게 보일지를 기준으로 정해야 한다.

## 📖 자세히

### 왜 경계에서 변환할까
외부에서 오는 값(예: 제휴사가 보내는 장르 코드 `01`, `02`)은 우리 내부 코드(`FICTION`, `COMIC`)와 다를 수 있다. 이 변환을 여기저기서 하면 비교 로직마다 "이 값은 외부 코드인가 내부 코드인가"를 따져야 하고, 매핑이 바뀔 때 고칠 곳이 흩어진다.

그래서 요청을 받는 입구(컨트롤러, 필터, 어댑터)에서 딱 한 번 내부 코드로 바꾸고, 그 안쪽은 내부 코드만 다룬다. 이렇게 외부 규격의 영향을 입구에 가두는 방식을 흔히 '변환 계층(anti-corruption layer)'이라고 부른다.

### 없는 값은 어떻게 할까
- lenient: 매핑에 없으면 입력값을 그대로 내부 코드로 본다. 외부가 이미 내부 코드를 보내는 경우도 통과해서 도입이 쉽다. 대신 오타도 그대로 흘러가므로, 뒤쪽에서 "해당 코드 없음"으로 안전하게 처리돼야 한다.
- strict: 매핑에 없으면 즉시 실패시킨다. 잘못된 값이 안쪽으로 못 들어오지만, 매핑 누락이 곧 장애가 된다.

```java
public record GenreCodeMapper(Map<String, String> mapping) {
    // lenient: 매핑에 없으면 입력값을 내부 코드로 간주
    public String toInternal(String external) {
        return mapping.getOrDefault(external, external);
    }

    // strict: 매핑에 없으면 예외로 입구에서 막는다
    public String toInternalStrict(String external) {
        String code = mapping.get(external);
        if (code == null) throw new IllegalArgumentException("unknown genre: " + external);
        return code;
    }
}
```

### 정리
변환은 입구에서 한 번, 안쪽은 내부 코드만. 정책(lenient/strict)은 한 메서드에 이름으로 드러내서 호출하는 쪽이 의도를 알 수 있게 한다.

## 🔎 더 공부할 것

- Anti-corruption layer - 외부 모델이 내부 도메인을 오염시키지 않게 막는 패턴
- Map.getOrDefault vs computeIfAbsent - 기본값 처리와 값 채우기의 차이
- Fail-fast vs graceful degradation - 잘못된 입력에 실패할지 계속 진행할지 고르는 기준

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#mapping` `#boundary` `#design` `#fallback` | 2026-10-13 | [예제 보기](../../example/java/external-code-mapping-at-boundary) |
