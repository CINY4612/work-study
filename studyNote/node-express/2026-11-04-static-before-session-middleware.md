<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [express, session, middleware, static, performance]
date: 2026-11-04
example: example/node-express/static-before-session-middleware
status: auto
-->

# 정적 파일은 세션 미들웨어보다 앞에 두기

> [!NOTE]
> express.static을 session보다 앞에 두면 js/css/이미지 요청이 세션 저장소(DB)를 조회하지 않고 바로 응답된다.

> [!WARNING]
> **헷갈린 점** · 미들웨어는 등록 순서대로 실행되고, static이 파일을 찾으면 응답을 끝내서 뒤의 미들웨어는 호출되지 않는다.

## 📖 자세히

### 왜 순서가 성능을 바꿀까
미들웨어(요청을 가로채 처리하는 함수)는 `app.use`로 등록한 순서대로 실행됩니다. `express.static`은 파일을 찾으면 응답을 보내고 `next()`를 부르지 않아서, 뒤에 있는 미들웨어는 아예 실행되지 않습니다.

1. 세션 미들웨어가 앞에 있으면 쿠키가 붙은 모든 요청에서 세션 저장소를 읽습니다(`get`), 만료 갱신(`touch`)도 일어납니다.
2. 저장소가 메모리면 비용이 거의 없어서 티가 안 납니다.
3. 저장소를 DB로 바꾸면 화면 하나에 리소스가 수십 개라서 DB 조회가 그만큼 늘어납니다.
4. static을 앞에 두면 파일 요청은 세션까지 오지 않습니다.

```ts
// 좋은 순서: 정적 파일이 먼저 응답하고 끝낸다
app.use(express.static(dir));
app.use(session({ store, resave: false, saveUninitialized: false }));

// 나쁜 순서: 정적 요청도 세션 조회를 거친다
// app.use(session(...)); app.use(express.static(dir));
```

### 같이 바꾸면 좋은 옵션
- `resave: false`: 바뀌지 않은 세션을 다시 저장하지 않습니다.
- `saveUninitialized: false`: 비어 있는 세션을 저장소에 만들지 않습니다.

### 주의점
- static이 앞에 있으므로 정적 파일에는 세션 기반 로그인 검사가 적용되지 않습니다. 인증이 필요한 파일은 static으로 서빙하면 안 됩니다.
- `public/index.html`이 있으면 `/` 요청을 static이 먼저 응답해서 뒤의 `app.get('/')` 라우트를 건너뜁니다.

## 🔎 더 공부할 것

- express-session store의 get/set/touch - 요청마다 저장소를 어떻게 부르는지 알아두기
- Express 미들웨어 실행 순서 - next() 호출 여부가 흐름을 결정한다
- https://expressjs.com/en/guide/using-middleware.html - 공식 미들웨어 가이드

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#express` `#session` `#middleware` `#static` `#performance` | 2026-11-04 | [예제 보기](../../example/node-express/static-before-session-middleware) |
