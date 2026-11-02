<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [spring, restcontroller, redirect, mvc]
date: 2026-11-02
example: example/spring-boot/restcontroller-vs-rest-api
status: auto
-->

# @RestController와 REST API는 다른 말이다

> [!NOTE]
> @RestController는 반환값을 뷰 이름이 아닌 응답 본문으로 쓴다는 뜻(@Controller+@ResponseBody)이고, REST API 여부와는 무관하다.

> [!WARNING]
> **헷갈린 점** · 본문 없이 302 Location만 주는 리다이렉트 엔드포인트도 @RestController로 만들 수 있어 이름만 보고 REST로 오해하기 쉽다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
`@RestController`는 `@Controller`와 `@ResponseBody`를 합친 애노테이션이다. `@ResponseBody`는 메서드의 반환값을 뷰(템플릿) 이름으로 해석하지 않고 HTTP 응답 본문에 직접 쓰라는 표시다. 이름에 REST가 들어가 있지만 "이 컨트롤러는 REST API다"를 선언하는 것은 아니다.

### 동작 원리
1. 요청이 `DispatcherServlet`(Spring MVC의 앞단 진입점)에 도착한다.
2. 핸들러 메서드가 실행되고 반환값이 나온다.
3. `@Controller`이면 `String` 반환값을 **뷰 이름**으로 보고 ViewResolver가 화면을 찾는다.
4. `@RestController`이면 반환값을 **HttpMessageConverter**가 본문으로 변환해 쓴다. 문자열이면 그대로 본문이 된다.
5. 반환 타입이 `ResponseEntity`이면 어느 쪽이든 상태 코드, 헤더, 본문을 그대로 쓴다. 그래서 `ResponseEntity<Void>`만 쓰는 컨트롤러는 `@Controller`로 바꿔도 동작이 같다.

```text
요청 -> DispatcherServlet -> 핸들러 메서드 -> 반환값
  @Controller      + String         -> 뷰 이름 -> 화면(HTML)
  @RestController  + String         -> 본문 텍스트
  (둘 다)          + ResponseEntity -> 상태/헤더/본문 그대로
```

### 구성 요소/용어 정리
| 용어 | 뜻 |
|---|---|
| `@ResponseBody` | 반환값을 뷰 이름이 아니라 본문으로 쓴다 |
| ViewResolver | 뷰 이름을 실제 화면으로 바꾸는 객체 |
| 리다이렉트 엔드포인트 | 본문 없이 302 + `Location` 헤더로 브라우저를 다른 주소로 보내는 진입점 |

### 코드로 보기
```java
// 본문 없이 302만 주는 진입점. 애노테이션이 @RestController여도 REST API는 아니다.
@RestController
class EntryController {
    @GetMapping("/enter")
    ResponseEntity<Void> enter() {
        return ResponseEntity.status(302).header("Location", "/pages/home").build();
    }
}

// @Controller + String = 뷰 이름. 화면은 ViewResolver가 찾는다.
@Controller
class PageController {
    @GetMapping("/home")
    String home() { return "pages"; }
}

// @RestController + String = 본문 그대로
@RestController
class TextController {
    @GetMapping("/text")
    String text() { return "pages"; }
}
```

### 비교
| 구분 | REST API | 리다이렉트 엔드포인트 |
|---|---|---|
| 호출 주체 | 프로그램(프론트 JS, 다른 서버) | 사용자 브라우저의 주소 이동 |
| 응답 | 리소스 표현(JSON 등) | 본문 없는 302 |
| URL의 의미 | 리소스 | 진입 경로 |

### 자주 하는 실수와 주의점
- 애노테이션 이름만 보고 문서에 "REST API"로 분류한다. 페이지 이동용이면 "HTTP 엔드포인트"나 "리다이렉트 엔드포인트"라고 쓰면 무난하다.
- `@RestController`에서 화면 이름을 문자열로 반환하면 화면이 아니라 그 글자가 응답으로 나간다.

### 세션에서 나온 질문 정리
**Q. 페이지를 내려주는 컨트롤러도 REST API라고 할 수 있나?**
A. 엄밀히 REST는 웹 전체의 아키텍처 스타일이라 HTML도 리소스 표현이 될 수 있다. 하지만 실무에서 REST API는 대개 프로그램이 호출하는 JSON API를 뜻하므로 구분해서 부르는 편이 정확하다.

**Q. `@RestController`를 `@Controller`로 바꾸면 동작이 달라지나?**
A. 반환 타입이 `ResponseEntity`면 같다. `String` 반환이면 달라진다.

## 🔎 더 공부할 것

- @ResponseBody - 반환값을 본문으로 쓰는 원리와 HttpMessageConverter
- ViewResolver - 뷰 이름이 화면으로 바뀌는 과정
- RedirectView와 redirect: 접두사 - 리다이렉트를 만드는 다른 방법

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#spring` `#restcontroller` `#redirect` `#mvc` | 2026-11-02 | [예제 보기](../../example/spring-boot/restcontroller-vs-rest-api) |
