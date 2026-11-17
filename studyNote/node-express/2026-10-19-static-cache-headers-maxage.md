<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [express, static, cache-control, etag, 304]
date: 2026-10-19
example: example/node-express/static-cache-headers-maxage
status: auto
-->

# express.static의 기본 캐시 헤더와 maxAge

> [!NOTE]
> express.static은 기본으로 Cache-Control: public, max-age=0과 ETag를 보내서, 브라우저가 매번 변경 여부만 확인(304)한다.

> [!WARNING]
> **헷갈린 점** · maxAge를 주면 그 시간 동안 서버에 묻지 않고 캐시를 쓰므로, 파일을 고쳐도 화면이 안 바뀌는 것처럼 보일 수 있다.

## 📖 자세히

### 왜 이렇게 동작할까
브라우저는 응답의 `Cache-Control`과 검증용 헤더(`ETag`, `Last-Modified`)를 보고 다시 요청할지 정한다.

1. `express.static` 기본값은 `max-age=0`이다. '캐시는 해도 되지만 곧바로 낡은 것으로 본다'는 뜻이라, 브라우저는 다음 요청에 `If-None-Match: <ETag>`를 붙여 서버에 묻는다.
2. 파일이 그대로면 서버는 본문 없이 `304 Not Modified`만 돌려주고, 브라우저는 저장해 둔 본문을 쓴다. 파일이 바뀌었으면 ETag가 달라져 `200`과 새 본문을 받는다.
3. `maxAge: '1h'`처럼 설정하면 `max-age=3600`이 붙는다. 이 1시간 동안에는 서버에 묻지도 않으므로, 배포로 파일을 바꿔도 사용자가 이전 파일을 볼 수 있다.

그래서 '고쳤는데 화면이 그대로'일 때는 응답 헤더(개발자도구 Network 탭)에서 `Cache-Control`과 '디스크 캐시에서 제공' 여부를 먼저 확인한다. 헤더 설정이 아니라 클라이언트가 만든 요청 방식(예: 캐시를 막지 않은 fetch나 load 호출) 때문일 수도 있으므로, 원인은 확인하고 단정한다.

```ts
// 기본: max-age=0 + ETag -> 매번 조건부 요청(304)
app.use(express.static(dir));

// 오래 캐시: 파일 이름에 해시가 붙은 정적 자원에만 쓴다
app.use('/assets', express.static(dir, { maxAge: '1h', immutable: true }));

// 클라이언트에서 확인
// 두 번째 요청에 If-None-Match를 붙이면 304가 돌아온다
```

### 실무 기준
- 이름이 고정된 HTML은 기본값(항상 검증)을 유지한다.
- 해시가 붙은 JS/CSS처럼 내용이 바뀌면 이름도 바뀌는 파일만 길게 캐시한다.
- 이름이 고정된 파일을 길게 캐시했다면 `app.js?v=2`처럼 쿼리로 버전을 붙여 우회할 수 있다.

## 🔎 더 공부할 것

- Cache-Control 지시자 - no-cache와 no-store는 서로 다르다
- ETag와 조건부 요청 - If-None-Match, 304의 흐름
- 캐시 무효화(cache busting) - 파일명 해시와 쿼리 버전
- https://expressjs.com/en/starter/static-files.html - express.static 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#express` `#static` `#cache-control` `#etag` `#304` | 2026-10-19 | [예제 보기](../../example/node-express/static-cache-headers-maxage) |
