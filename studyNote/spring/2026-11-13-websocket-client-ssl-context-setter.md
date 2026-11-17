<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [websocket, ssl, tomcat, spring, sslcontext]
date: 2026-11-13
example: example/spring-boot/websocket-client-ssl-context-setter
status: auto
-->

# WebSocket 클라이언트의 SSLContext는 전용 setter로 지정

> [!NOTE]
> Tomcat 내부 user property 키로 SSLContext를 넘기는 방식은 버전이 바뀌면 조용히 무시될 수 있어, Spring의 setSslContext()처럼 공식 API를 쓴다.

> [!WARNING]
> **헷갈린 점** · 무시되면 오류 없이 JDK 기본 인증서 검증이 돌아, 호스트명이 인증서와 다를 때(localhost 등) 연결이 실패한다.

## 📖 자세히

### 왜 이렇게 동작할까
`StandardWebSocketClient`는 JSR-356(자바 표준 WebSocket API) 위에서 동작하고, 실제 연결은 Tomcat 같은 구현체가 한다. 예전에는 구현체 전용 키(`org.apache.tomcat.websocket.SSL_CONTEXT`)를 user property에 넣어 SSLContext를 전달했다. 하지만 user property는 `Map<String,Object>`라서 키 이름이 틀리거나 구현체가 그 키를 더 이상 읽지 않아도 컴파일 오류도 예외도 없다.

1. 구현체 업그레이드로 해당 키 지원이 사라진다.
2. 넣은 SSLContext는 조용히 무시된다.
3. JDK 기본 TrustManager가 인증서와 호스트명을 검증한다.
4. 인증서의 SAN(Subject Alternative Name)에 접속 호스트가 없으면 핸드셰이크가 실패한다.

전용 setter는 타입이 있는 메서드라서 사라지면 컴파일 단계에서 바로 드러난다.

```java
// 나쁜 예: 문자열 키는 지원이 끊겨도 아무 신호가 없다
client.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", sslContext);

// 좋은 예: 타입이 있는 API. 없어지면 컴파일 오류
client.setSslContext(sslContext);
```

### 주의
인증서 검증을 끄는 trust-all SSLContext는 로컬 개발 확인용으로만 쓴다. 운영에서는 올바른 호스트명의 인증서나 신뢰 저장소를 구성한다.

## 🔎 더 공부할 것

- JSR-356 user properties - 구현체별 키가 무시될 때의 위험
- SAN(Subject Alternative Name) - 호스트명 검증이 인증서의 어떤 필드를 보는지
- https://docs.spring.io/spring-framework/reference/web/websocket.html - Spring WebSocket 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#websocket` `#ssl` `#tomcat` `#spring` `#sslcontext` | 2026-11-13 | [예제 보기](../../example/spring-boot/websocket-client-ssl-context-setter) |
