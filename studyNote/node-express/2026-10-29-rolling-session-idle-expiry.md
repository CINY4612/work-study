<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [session, cookie, rolling, maxAge, express-session]
date: 2026-10-29
example: example/node-express/rolling-session-idle-expiry
status: auto
-->

# rolling 세션으로 브라우저 종료 후 유휴 만료

> [!NOTE]
> 브라우저를 그냥 닫으면 로그아웃 요청이 안 가므로, rolling과 짧은 maxAge로 마지막 요청 이후 일정 시간이 지나면 만료시킨다.

> [!WARNING]
> **헷갈린 점** · maxAge가 길고 rolling이 없으면 로그인 시각 기준 고정 만료라 닫았다 다시 열어도 로그인이 유지된다.

## 📖 자세히

### 왜 이렇게 동작할까
서버는 브라우저 종료를 알 방법이 없다. 그래서 "마지막 요청 이후 일정 시간이 지나면 만료"(유휴 타임아웃, idle timeout)로 처리한다.

- `maxAge`가 있는 쿠키는 영속 쿠키라 브라우저를 닫아도 남는다. 시간이 남아 있으면 다시 열었을 때 로그인 상태다.
- `rolling: false`(기본값)이면 만료 시각은 로그인 시점으로 고정된다.
- `rolling: true`이면 요청마다 만료 시각이 `now + maxAge`로 밀린다. 화면이 주기적으로 요청(heartbeat)을 보내면 열려 있는 동안 세션이 유지되고, 닫으면 maxAge 뒤에 만료된다.
- 같은 계정의 세션이 둘 생기는 것도 정상이다. `regenerate()`는 요청에 실린 쿠키의 세션만 지우기 때문이다.

```ts
// express-session 설정의 핵심 두 줄
app.use(session({
  secret: process.env.SESSION_SECRET!, // 서명용 비밀값은 환경변수로
  rolling: true,                 // 요청마다 만료 시각 연장
  cookie: { maxAge: 30 * 60_000 } // 마지막 요청 후 30분 지나면 만료
}));
```

주의: 주기 요청이 없는 화면은 조작이 없으면 maxAge 뒤 로그아웃된다. 해당 화면에도 가벼운 heartbeat를 넣어야 한다.

## 🔎 더 공부할 것

- express-session cookie.expires와 store.touch - 스토어가 만료 시각을 갱신하는 시점
- session.regenerate - 로그인 시 세션 고정 공격 방지
- 만료 세션 정리 스케줄러 - 남은 row를 주기적으로 지우는 방식

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#session` `#cookie` `#rolling` `#maxAge` `#express-session` | 2026-10-29 | [예제 보기](../../example/node-express/rolling-session-idle-expiry) |
