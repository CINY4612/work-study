<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [jwt, session, auth, revocation]
date: 2026-10-02
example: example/node-express/session-vs-jwt-force-logout
status: auto
-->

# 서버 세션과 JWT는 강제 로그아웃 가능 여부가 왜 다른지 이해하는 법

> [!NOTE]
> 서버 세션은 저장소에서 행을 지우면 다음 요청부터 차단되지만, JWT는 서버가 상태를 안 갖기 때문에 만료 시각까지 유효하다.

> [!WARNING]
> **헷갈린 점** · '빈 토큰을 응답으로 내려 갱신'은 클라이언트가 협조할 때만 통하고, 탈취된 토큰은 그대로 쓸 수 있다.

## 📖 자세히

### 왜 이렇게 동작할까
- **서버 세션**: 쿠키에는 세션 ID만 있고 실제 로그인 상태는 서버 저장소(메모리, DB, Redis)에 있다. 요청마다 저장소를 조회하므로 상태를 지우면 곧바로 로그아웃된다.
- **JWT**(JSON Web Token): 사용자 정보와 만료 시각을 서명해서 토큰 안에 담는다. 서버는 서명만 검증하면 되므로 저장소 조회가 없고, 그만큼 서버가 '이 토큰을 무효로 하겠다'고 말할 곳도 없다.
- 그래서 빈 토큰을 내려주는 방식은 정상 브라우저만 지운다. 이미 복사된 토큰은 서명과 만료가 유효하니 계속 통과한다.

### JWT에서 강제 로그아웃이 필요하다면
서버에 최소한의 상태를 다시 둬야 한다. 사용자별 `ver`(토큰 버전)를 토큰에 넣고, 강제 로그아웃 때 서버의 버전을 올려 이전 토큰을 모두 거부한다. 이러면 요청마다 조회가 한 번 생겨 세션 방식과 비용이 비슷해진다.

```ts
// 발급 시 현재 버전을 토큰에 실어 보낸다
issue(userId: string) {
  const ver = this.versions.get(userId) ?? 0;
  return signJwt({ sub: userId, exp: now + 60, ver }, SECRET);
}
// 검증: 서명 + 만료 + 서버가 아는 최신 버전과 일치하는지
check(token: string) {
  const c = verifyJwt(token, SECRET);
  return !!c && c.ver === (this.versions.get(c.sub) ?? 0);
}
// 강제 로그아웃: 버전만 올리면 기존 토큰이 전부 무효
forceLogout(userId: string) {
  this.versions.set(userId, (this.versions.get(userId) ?? 0) + 1);
}
```

### 선택 기준
| 항목 | 서버 세션 | JWT |
|---|---|---|
| 강제 만료 | 즉시 | 별도 상태 필요 |
| 요청당 비용 | 저장소 조회 | 서명 검증만 |
| 저장소 장애 | 인증 실패 | 영향 적음 |

강제 만료가 필수인 서비스라면 세션이 단순하고, 서버 간 확장과 무상태가 중요하면 JWT에 짧은 만료를 둔다.

## 🔎 더 공부할 것

- Refresh Token - 짧은 Access Token과 함께 쓰면 폐기 지점을 refresh 쪽에 둘 수 있다
- 토큰 denylist(jti) - 버전 방식 대신 개별 토큰을 막는 방법
- express-session 저장소 - 메모리 저장소가 운영에 부적합한 이유
- https://expressjs.com/ - 미들웨어에서 인증을 붙이는 위치

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#jwt` `#session` `#auth` `#revocation` | 2026-10-02 | [▶ 실행해보기](../../example/node-express/session-vs-jwt-force-logout) |
