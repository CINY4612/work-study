<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [redirect, login, load-balancing, fallback, timeout]
date: 2026-10-29
example: example/node-express/login-redirect-single-hop
status: auto
-->

# 로그인 후 배정 서버를 직접 물어 리다이렉트 한 번으로

> [!NOTE]
> 로그인 성공 시 서버가 배정기에 직접 물어 목적지를 알아내면 '정문 → 배정 서버' 2단 리다이렉트를 1번으로 줄일 수 있다.

> [!WARNING]
> **헷갈린 점** · 배정기 호출이 실패하거나 느리면 사용자를 막지 말고 타임아웃 후 정문 주소로 되돌리는 폴백(2단)을 남겨야 한다.

## 📖 자세히

### 왜 리다이렉트가 두 번이 되었나
리다이렉트는 서버가 `302 + Location`으로 브라우저에게 "다른 주소로 다시 요청하라"고 시키는 응답이다. 브라우저는 매번 왕복을 새로 하므로 단계가 늘수록 느리고, 중간에서 실패할 지점도 늘어난다.

```text
전: 로그인 -> 302 정문(배정기) -> 302 배정된 서버
후: 로그인 -> (서버끼리 배정 조회) -> 302 배정된 서버
```

정문이 하던 일은 "이 사용자를 어느 서버에 보낼지 고르는 것"이다. 이 판단을 로그인 처리 서버가 서버 간 호출(브라우저를 거치지 않는 HTTP 호출)로 먼저 받아 오면 브라우저는 한 번만 이동한다.

### 판단 순서
1. 진입 주소의 힌트(예: `lid`)가 로그인한 사용자와 같으면 이미 맞는 서버이므로 그대로 진행한다.
2. 다르면 배정기에게 사용자 기준으로 서버를 묻는다.
3. 배정된 서버가 현재 서버면 리다이렉트 없이 진행한다.
4. 다른 서버면 그 서버 주소로 바로 보낸다. (세션이 공유 저장소에 있어야 서버가 바뀌어도 로그인이 유지된다.)
5. 배정기 호출이 실패/타임아웃이면 정문 주소로 보낸다. 느리지만 정문이 알아서 배정해 주므로 사용자는 막히지 않는다.

### 코드로 보기
```ts
export async function resolveLoginRedirect(i: Input): Promise<string> {
  // 1) 이미 이 사용자에게 맞춘 진입이면 이동 없음
  if (i.entryHint === i.userId) return '/index.html';
  try {
    // 2) 서버끼리 묻되, 오래 걸리면 포기한다
    const a = await withTimeout(i.allocate(i.userId), i.timeoutMs ?? 3000);
    // 3) 내가 배정됐다면 이동 불필요
    if (a.serverId === i.selfId) return '/index.html';
    // 4) 한 번에 배정 서버로
    return `${a.baseUrl}/main?hint=${encodeURIComponent(i.userId)}`;
  } catch {
    // 5) 폴백: 정문이 대신 배정 (리다이렉트 2단)
    return `${i.frontDoorUrl}/main?hint=${encodeURIComponent(i.userId)}`;
  }
}
```

### 주의점
- 폴백 없이 배정기 오류를 그대로 던지면 배정기 장애가 곧 로그인 장애가 된다.
- 타임아웃 타이머는 `finally`에서 `clearTimeout` 해야 테스트/프로세스가 안 끝나는 일이 없다.
- 배정기에 필요한 정보(예: 사용자가 쓰는 서비스 목록)를 로그인 쪽이 이미 조회했다면 요청에 같이 실어 보내 중복 조회를 없앤다. 그 정보를 얻지 못했다면 빈 값으로 보내지 말고 폴백으로 가야 한다. 빈 값은 '전체 대상'으로 해석되어 잘못 배정될 수 있기 때문이다.

## 🔎 더 공부할 것

- 302 vs 307/308 리다이렉트 - 메서드 유지 여부가 다르다
- AbortController - fetch 타임아웃을 정석적으로 거는 법
- 공유 세션 저장소 - 서버가 바뀌어도 로그인이 유지되는 조건
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Redirections - 리다이렉트 동작 개요

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#redirect` `#login` `#load-balancing` `#fallback` `#timeout` | 2026-10-29 | [예제 보기](../../example/node-express/login-redirect-single-hop) |
