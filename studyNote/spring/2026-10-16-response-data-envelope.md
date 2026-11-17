<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [jackson, JsonAnyGetter, response, envelope]
date: 2026-10-16
example: example/spring-boot/response-data-envelope
status: auto
-->

# 응답 부가 데이터는 data 안에 담기

> [!NOTE]
> 공통 응답에서 `@JsonAnyGetter`는 Map을 최상위로 펼쳐 상태 필드와 섞이고, `data` 필드로 감싸면 구조가 분리된다.

> [!WARNING]
> **헷갈린 점** · 형식을 바꾸면 옛 구조를 읽던 소비자는 오류 없이 값을 못 읽을 수 있어 호출하는 쪽도 같이 고쳐야 한다.

## 📖 자세히

### 왜 이렇게 동작할까
`@JsonAnyGetter`(Map의 엔트리를 JSON 최상위 속성으로 펼치는 Jackson 어노테이션)를 쓰면 `resultCode` 같은 고정 필드와 가변 데이터가 같은 층에 놓인다. 그러면 키 충돌 가능성이 생기고, 어디까지가 공통 필드인지 구분하기 어렵다. `data` 필드 하나로 감싸면 공통 필드와 업무 데이터가 분리된다.

`@JsonInclude(NON_EMPTY)`를 붙이면 data가 비었을 때 `"data"` 키 자체가 빠진다. 항상 `"data":{}`를 내려주려면 이 어노테이션만 지우면 된다.

### 주의: 소비자 호환
JSON을 읽는 쪽은 `json.get("serverId")` 처럼 최상위에서 값을 읽을 수 있다. 응답을 `data` 아래로 옮기면 그 값이 null이 되지만 예외는 나지 않는다. 서버 간 호출(health check 등)이 있으면 호출하는 클라이언트도 같이 고쳐야 한다. 테스트에서 `$.data.serverId`가 있는지와 최상위에 `serverId`가 없는지를 둘 다 확인하면 안전하다.

```java
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Envelope(int resultCode, String resultMsg, Map<String, Object> data) {}
// {"resultCode":200,"resultMsg":"OK","data":{"serverId":"1"}}
```

## 🔎 더 공부할 것

- @JsonInclude - NON_NULL, NON_EMPTY, ALWAYS의 차이
- 소비자 주도 계약 테스트 - 응답 형식 변경을 배포 전에 감지하기
- @RestControllerAdvice - 실패 응답도 같은 envelope로 맞추기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#jackson` `#JsonAnyGetter` `#response` `#envelope` | 2026-10-16 | [예제 보기](../../example/spring-boot/response-data-envelope) |
