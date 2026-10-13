<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [express, security, allowlist, fail-closed]
date: 2026-10-13
example: example/node-express/ip-allowlist-fail-closed
status: auto
-->

# IP 허용 목록은 설정이 없으면 전부 막기

> [!NOTE]
> 내부 전용 API는 허용 IP 목록으로 거르고, 설정이 없거나 비어 있으면 모두 거부(fail-closed)해야 설정 누락이 보안 구멍이 되지 않는다.

> [!WARNING]
> **헷갈린 점** · req.ip는 IPv4가 '::ffff:127.0.0.1'처럼 IPv6 형태로 올 수 있어, 비교 전에 접두사를 떼지 않으면 정상 호출도 403이 된다.

## 📖 자세히

### 왜 이렇게 동작할까

- **fail-open**: 허용 목록이 없으면 검사를 건너뛰는 방식. 설정 파일에서 키 하나가 빠지면 모든 호출이 통과한다.
- **fail-closed**: 목록이 없거나 비어 있으면 전부 거부한다. 설정이 빠지면 '서비스가 안 된다'는 눈에 띄는 장애가 되고, 조용한 보안 구멍은 생기지 않는다.
- Node는 IPv4/IPv6 겸용 소켓으로 listen하면 IPv4 클라이언트를 IPv4-mapped IPv6 주소(`::ffff:a.b.c.d`)로 보여 준다. 그래서 문자열을 그대로 비교하면 어긋난다.
- 프록시 뒤에 있다면 `trust proxy` 설정 없이는 `req.ip`가 프록시 주소다. 이 예제는 직접 연결만 다룬다.

```ts
export function ipAllowlist(allowed: string[] | undefined): RequestHandler {
  return (req, res, next) => {
    const ip = normalizeIp(req.ip);
    // 설정이 없거나 비어 있으면 includes가 false -> 전부 403 (fail-closed)
    if (!allowed || !allowed.includes(ip)) {
      res.status(403).json({ resultCode: 403 });
      return;
    }
    next();
  };
}
```

## 🔎 더 공부할 것

- app.set('trust proxy') - 프록시/로드밸런서 뒤에서 req.ip가 달라지는 이유
- CIDR 대역 허용 - IP 하나씩 나열하는 방식의 한계
- https://expressjs.com/en/guide/behind-proxies.html - Express 프록시 설정 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#express` `#security` `#allowlist` `#fail-closed` | 2026-10-13 | [예제 보기](../../example/node-express/ip-allowlist-fail-closed) |
