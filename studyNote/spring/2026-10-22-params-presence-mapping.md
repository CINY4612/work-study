<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [requestmapping, params, mockmvc]
date: 2026-10-22
example: example/spring-boot/params-presence-mapping
status: auto
-->

# @GetMapping params로 쿼리 키 존재만 매칭하기

> [!NOTE]
> params = "key=1"은 값까지 일치해야 매칭되고, params = "key"는 키만 있으면 매칭된다. 값 검증은 메서드 안에서 한다.

> [!WARNING]
> **헷갈린 점** · 같은 경로에 조건 없는 매핑을 하나 더 두면 기동 시 모호한 매핑 오류가 난다. 조건이 있는 쪽이 더 구체적이라 우선한다.

## 📖 자세히

### 왜 이렇게 동작할까
Spring MVC는 같은 경로의 후보가 여러 개이면 조건이 더 구체적인 매핑을 고른다. `params`가 붙은 매핑이 조건 없는 매핑보다 우선한다.

- `params = "serverError=1"`: 값이 1일 때만 이 메서드로 온다. 다른 값(`=abc`)이면 조건 없는 일반 매핑으로 떨어져 엉뚱한 화면이 나온다.
- `params = "serverError"`: 키만 있으면 이 메서드로 온다. 값이 1이 아닐 때 404를 줄지는 메서드 안에서 결정한다.
- 이때 `@RequestParam`은 `required = false`로 받는다. `?serverError`처럼 값이 비면 필수 파라미터는 null 변환 실패로 400이 나기 때문이다.
- 같은 경로에 `params`가 없는 매핑을 둘 다 두면 `Ambiguous mapping` 오류로 기동이 실패하므로, 한쪽에는 반드시 `params` 같은 조건이 있어야 한다.

```java
@GetMapping(path = "/main", params = "serverError")
ResponseEntity<String> page(@RequestParam(name = "serverError", required = false) String v) {
    if ("1".equals(v)) return ResponseEntity.ok("server-error");
    return ResponseEntity.status(404).body("invalid"); // 잘못된 값은 404
}
```

## 🔎 더 공부할 것

- RequestMappingInfo - 매핑 조건 비교와 우선순위
- @RequestParam required/defaultValue - 파라미터 누락 처리
- MockMvc standaloneSetup - 컨트롤러만 띄워 테스트

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#requestmapping` `#params` `#mockmvc` | 2026-10-22 | [예제 보기](../../example/spring-boot/params-presence-mapping) |
