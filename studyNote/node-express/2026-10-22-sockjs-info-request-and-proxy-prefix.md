<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [sockjs, websocket, nginx, reverse-proxy, proxy_pass]
date: 2026-10-22
example: example/node-express/sockjs-info-request-and-proxy-prefix
status: auto
-->

# SockJS /info 사전 요청과 프록시 경로 prefix

> [!NOTE]
> SockJS 클라이언트는 웹소켓을 열기 전에 항상 GET {주소}/info?t=타임스탬프 를 먼저 보내고, 이게 실패하면 연결 자체가 시작되지 않는다.

> [!WARNING]
> **헷갈린 점** · 프록시의 /1/ 같은 prefix는 proxy_pass 끝에 /가 있어야 떼어지고, 없으면 prefix가 붙은 채 서버에 도착해 404가 난다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
**SockJS**는 브라우저의 WebSocket이 막힌 환경(프록시, 방화벽)에서도 연결되도록 여러 전송 방식(websocket, xhr-streaming, xhr-polling 등)을 자동으로 고르는 클라이언트/서버 라이브러리다. 어떤 전송 방식을 쓸 수 있는지 알아야 하므로, 실제 연결 전에 서버에 먼저 물어본다. 그 질문이 `/info` 요청이다.

### 동작 원리
1. 클라이언트가 `GET {접속 URL}/info?t={현재 시각(ms)}`를 보낸다. `t`는 캐시를 피하려고 붙이는 값이다.
2. 서버는 `{ websocket: true, cookie_needed: false, origins: [...], entropy: ... }` 같은 JSON으로 답한다.
3. 클라이언트는 이 응답으로 전송 방식을 정한다.
4. 정해진 뒤에야 `{접속 URL}/{server}/{session}/websocket` 같은 실제 연결을 연다.

```text
브라우저 -> GET /1/ws/info -> [프록시 location /1/] -> GET /ws/info -> 앱 서버
                                  (prefix를 떼어야 함)
```

따라서 `/info`가 404면 "웹소켓이 안 되는 것"이 아니라 "연결 시도조차 못 한 것"이다.

### 구성 요소/용어 정리
| 용어 | 의미 |
|---|---|
| prefix | 프록시가 여러 서버 중 하나로 보내려고 URL 앞에 붙인 구분자 (예: `/1`) |
| location | nginx에서 특정 경로로 들어온 요청을 처리하는 설정 블록 |
| proxy_pass | 요청을 넘길 업스트림 서버 주소. 끝에 URI(`/`)가 있느냐에 따라 동작이 다르다 |
| 업스트림 | 프록시 뒤에 있는 실제 앱 서버 |

### 코드로 보기
아래 예제는 nginx의 `proxy_pass` 규칙을 흉내 낸 함수다. `withUri`가 true면 prefix를 떼고, false면 그대로 넘긴다.

```ts
export function proxyPass(prefix: string, upstream: RequestListener, withUri: boolean): RequestListener {
  return (req, res) => {
    // 이 location에 해당하지 않는 요청은 프록시에서 404
    if (!req.url?.startsWith(prefix + '/')) { res.statusCode = 404; res.end(); return; }
    // proxy_pass http://up/;  (끝에 / 있음) -> location 부분을 떼고 넘긴다
    // proxy_pass http://up;   (끝에 / 없음) -> 원래 URI를 그대로 넘긴다
    if (withUri) req.url = req.url.slice(prefix.length);
    upstream(req, res);
  };
}
```

```nginx
location /1/ {
    proxy_pass https://app-node1:4883/;   # 끝의 / 때문에 /1/ 이 떼어진다
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;   # 웹소켓 업그레이드용
    proxy_set_header Connection "upgrade";
}
```

### 비교
| 설정 | 요청 `/1/ws/info` 가 업스트림에 도착하는 경로 |
|---|---|
| `proxy_pass http://up/;` | `/ws/info` |
| `proxy_pass http://up;` | `/1/ws/info` (그대로) |

### 자주 하는 실수와 주의점
- 앱 서버에 `/ws`만 등록되어 있는데 prefix가 붙은 채 도착하면 404가 난다. 앱 로그에서 prefix 포함 경로가 보이면 프록시를 의심한다.
- 같은 location을 타는 REST API 경로도 같은 이유로 함께 404가 난다.
- 프록시를 아예 거치지 않고 앱 포트로 직접 가는 경로(설정 파일 캐시, 잘못된 주소)도 같은 증상을 만든다. 브라우저 Network 탭에서 `info` 요청의 전체 URL(호스트·포트)부터 확인한다.
- 설정 파일을 고친 뒤 nginx를 reload하지 않았으면 이전 설정이 적용 중이다.

### 세션에서 나온 질문 정리
**Q. 서버 로그에 내가 부르지 않은 `/info` 요청이 찍히는데 어디서 나온 건가?**
A. SockJS 클라이언트가 연결 전에 자동으로 보내는 사전 요청이다.

**Q. 브라우저는 왜 `/info`를 요청하나?**
A. 서버가 웹소켓을 지원하는지, 쿠키가 필요한지 확인해 전송 방식을 고르기 위해서다.

**Q. 주소 설정은 정상인데 연결이 안 되는 이유는?**
A. 주소의 호스트·포트가 맞아도 경로 prefix가 업스트림에 그대로 전달되면 `/info`가 404가 되어 연결이 시작되지 않는다.

## 🔎 더 공부할 것

- proxy_pass의 URI 처리 규칙 - prefix를 떼는 조건을 정확히 알아야 한다
- WebSocket Upgrade 헤더 - 프록시 뒤에서 웹소켓이 동작하는 방식
- SockJS 전송 방식 fallback - websocket이 막힐 때 어떤 방식으로 대체되는지
- https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API - WebSocket 기본 동작

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#sockjs` `#websocket` `#nginx` `#reverse-proxy` `#proxy_pass` | 2026-10-22 | [예제 보기](../../example/node-express/sockjs-info-request-and-proxy-prefix) |
