<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [ejs, express, template, locals, res.render]
date: 2026-11-11
example: example/node-express/ejs-undefined-variable-locals
status: auto
-->

# ejs에서 넘기지 않은 변수는 locals로 확인하기

> [!NOTE]
> ejs 템플릿에서 res.render로 넘기지 않은 변수를 그냥 참조하면 ReferenceError가 나고, locals.변수명은 undefined가 되어 안전하다.

> [!WARNING]
> **헷갈린 점** · 같은 템플릿을 여러 라우트가 렌더링할 때, 한 라우트만 변수를 넘기면 다른 라우트에서 터진다. 라이브러리 버전 문제가 아니다.

## 📖 자세히

### 왜 이렇게 동작할까
ejs는 템플릿을 JS 함수로 컴파일하고, res.render에 넘긴 객체를 `with (locals) { ... }` 안에서 실행한다. (`with`는 객체의 속성을 변수처럼 쓰게 해 주는 문법이다.) 그래서 넘긴 키는 평범한 변수처럼 보이지만, 넘기지 않은 키는 선언된 적 없는 변수가 되어 `ReferenceError: x is not defined`가 난다.

`locals`는 항상 존재하는 객체이므로 `locals.x`는 없는 속성을 읽는 것일 뿐이라 `undefined`가 된다. 에러가 아니다.

1. 라우트 A: `res.render('end', { notSupport: true })` -> 변수 존재
2. 라우트 B: `res.render('end')` -> 변수 없음
3. 템플릿의 `if (notSupport)` -> B에서 ReferenceError
4. 템플릿의 `if (locals.notSupport)` -> B에서 undefined, 거짓으로 처리

```ejs
<%# 잘못된 예: 넘기지 않으면 터진다 %>
<% if (notSupport) { %> ... <% } %>

<%# 올바른 예: 선택적 값은 locals로 읽는다 %>
<% if (locals.notSupport) { %> ... <% } %>
```

### 주의할 점
- 값이 없을 수 있는 변수만 locals로 읽으면 된다. 항상 넘겨야 하는 값이 빠졌을 때는 오히려 에러가 나서 버그를 빨리 알 수 있다.
- 템플릿을 공유하는 라우트가 여러 개면, 변수 목록을 템플릿이 쓰는 것과 비교해 보자.
- 라이브러리를 업그레이드하기 전에 원본 코드를 같은 버전으로 재현해 보면 원인이 버전인지 코드인지 가릴 수 있다.

## 🔎 더 공부할 것

- res.locals - 모든 템플릿에 공통 값을 넣는 방법
- ejs 옵션 _with / localsName - with 사용 여부와 locals 이름 설정
- https://expressjs.com/en/api.html#res.render - res.render 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#ejs` `#express` `#template` `#locals` `#res.render` | 2026-11-11 | [예제 보기](../../example/node-express/ejs-undefined-variable-locals) |
