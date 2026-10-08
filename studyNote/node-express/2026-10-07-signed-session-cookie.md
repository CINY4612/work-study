<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [session, cookie, hmac, express-session]
date: 2026-10-07
example: example/node-express/signed-session-cookie
status: auto
-->

# 서명된 sid 쿠키로 req.session이 복원되는 원리

> [!NOTE]
> 로그인 세션 데이터는 서버 저장소에만 있고, 브라우저는 서명된 sid 쿠키만 들고 있다가 매 요청마다 그 키로 세션을 찾아온다.

> [!WARNING]
> **헷갈린 점** · req.session은 localStorage와 무관하고, 세션에 아무것도 안 넣으면(saveUninitialized: false) 쿠키도 저장소 기록도 만들지 않는다.

## 📖 자세히

### 왜 이렇게 동작할까
HTTP는 요청마다 독립적이라 서버는 "이 요청이 아까 그 사용자"인지 모른다. 그래서 서버가 세션 ID(sid, 세션을 찾는 열쇠)를 만들고 `Set-Cookie` 응답 헤더로 브라우저에 맡긴다. 브라우저는 이후 같은 도메인 요청마다 `Cookie` 헤더로 sid를 자동으로 보낸다.

1. 요청이 오면 쿠키에서 sid를 꺼내 서명을 검증한다. 서명은 서버만 아는 secret으로 만든 HMAC(해시 기반 위변조 검출 값)이라, 클라이언트가 sid를 바꾸면 검증에서 탈락한다.
2. 검증된 sid로 저장소(메모리, DB 등)에서 데이터를 읽어 `req.session`에 채운다.
3. 응답 직전에 세션에 값이 들어 있을 때만 저장하고 쿠키를 내보낸다. 로그인 전 요청마다 세션을 만들면 저장소가 쓰레기로 차기 때문이다.
4. 로그인 성공 시에는 sid를 새로 발급(regenerate)한다. 로그인 전 sid를 공격자가 알고 있어도 무효가 되게 하려는 것(세션 고정 공격 방어)이다.

### 이름이 비슷한 세 가지
- `req.session`: 서버가 관리하는 객체. 브라우저는 내용을 못 본다.
- 세션 쿠키: sid만 든 브라우저 쿠키. `HttpOnly`면 JS가 읽지 못한다.
- `sessionStorage`/`localStorage`: 프론트 JS가 직접 넣는 저장소. 서버로 자동 전송되지 않고 로그인 세션과 연결도 없다.

```ts
// 서명: 값 + HMAC. 클라이언트가 값을 바꾸면 mac이 달라진다
export function sign(value: string, secret: string): string {
  const mac = createHmac('sha256', secret).update(value).digest('base64url');
  return `s:${value}.${mac}`;
}
// 검증은 다시 계산해서 timingSafeEqual로 비교 (문자열 === 는 시간차 공격에 약함)
```

## 🔎 더 공부할 것

- 세션 고정 공격(session fixation) - regenerate를 왜 로그인 직후 하는지
- 쿠키 속성 HttpOnly/Secure/SameSite - 각각 무엇을 막는지 https://developer.mozilla.org/docs/Web/HTTP/Cookies
- 세션 저장소를 DB로 옮길 때 - 만료 정리와 동시 쓰기 주의점

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#session` `#cookie` `#hmac` `#express-session` | 2026-10-07 | [예제 보기](../../example/node-express/signed-session-cookie) |
