<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [health-check, HttpClient, proxy, 502]
date: 2026-10-21
example: example/java/probe-http-status-as-alive
status: auto
-->

# 헬스 프로브에서 4xx/5xx 응답을 살아있음으로 보는 함정

> [!NOTE]
> 프로브가 '응답이 왔는가'만 보면, 앞단 프록시가 502를 돌려줄 때 뒤의 서버가 죽어 있어도 정상으로 판정된다.

> [!WARNING]
> **헷갈린 점** · 연결 거부·타임아웃일 때만 장애가 되므로, 프록시 뒤 대상은 5xx도 장애로 볼지 정해야 한다.

## 📖 자세히

### 왜 이렇게 동작할까
프로브를 '서버가 응답했는가(통신 가능)'로 만들면 HTTP 상태코드는 보지 않는다. 대상 앞에 nginx 같은 리버스 프록시가 있으면 뒤의 앱이 죽어도 프록시가 직접 502/503/504를 응답하므로, 프로브는 '살아있음'으로 판단한다. 연결 자체가 거부되거나 시간이 초과될 때만 장애가 된다.

```java
// 응답이 오면 상태코드와 무관하게 true
boolean isReachable(String url) {
  try { client.send(request(url), BodyHandlers.discarding()); return true; }
  catch (IOException e) { return false; } // 연결 거부, 타임아웃
}

// 502/503/504는 '뒤가 죽었다'는 신호로 보고 false
boolean isHealthy(String url) { /* 상태코드 확인 */ }
```

### 선택 기준
- 프로브를 프록시를 거치지 않는 내부 주소로 직접 보낼 수 있으면 가장 확실하다.
- 프록시를 거쳐야 하면 502/503/504를 장애로 본다. 단, 앱이 일시적으로 503을 내는 경우도 장애가 되는 점은 감안한다.

## 🔎 더 공부할 것

- Spring Boot Actuator health - 상태코드와 본문으로 상태를 표현하는 표준 방식
- 리버스 프록시 502/504 - 뒤 서버 장애가 어떤 코드로 나타나는지
- java.net.http.HttpClient - connectTimeout과 요청 timeout의 차이

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#health-check` `#HttpClient` `#proxy` `#502` | 2026-10-21 | [예제 보기](../../example/java/probe-http-status-as-alive) |
