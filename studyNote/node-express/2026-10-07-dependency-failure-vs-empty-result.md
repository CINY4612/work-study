<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [http-status, retry, error-handling, express]
date: 2026-10-07
example: example/node-express/dependency-failure-vs-empty-result
status: auto
-->

# 의존 서비스 장애는 503으로, 자리가 없다는 업무 결과는 200으로 구분해 응답하는 법

> [!NOTE]
> Redis·DB 같은 의존 서비스 장애와 '배정할 자리 없음'이라는 정상 업무 결과를 같은 응답으로 보내면 호출자가 원인을 구분할 수 없다.

> [!WARNING]
> **헷갈린 점** · 장애만 503 + 에러 코드로 내리고, 재시도는 일시 장애에만 건다. 자리 없음은 재시도해도 같은 답이라 바로 돌려준다.

## 📖 자세히

### 왜 구분해야 할까
호출자(다른 서버나 프론트)는 응답만 보고 다음 행동을 정한다. 장애와 '빈 자리 없음'이 둘 다 `200 NOT_ALLOCATED`면 이런 문제가 생긴다.

- 모니터링이 장애를 못 잡는다. 200만 오니 정상으로 보인다.
- 호출자는 '자리가 없다'고 믿고 사용자에게 잘못된 안내를 한다.
- 로드밸런서나 재시도 정책이 상태 코드(5xx)를 기준으로 동작하는데, 200이면 동작하지 않는다.

### 재시도는 무엇에 걸까
- 일시적인 장애(연결 끊김, failover 중): 잠깐 뒤 다시 하면 성공할 수 있으므로 간격을 두고 N회 재시도한다.
- 업무 결과(자리 없음): 다시 물어봐도 답이 같다. 재시도하면 부하만 늘어나므로 바로 반환한다.
- 그래서 '장애'는 예외(throw)로, '자리 없음'은 `null` 반환으로 표현해 호출부에서 갈라지게 만든다.

```ts
// 저장소 장애만 이 예외로 표현한다. null 반환은 '자리 없음'이라는 정상 결과다.
export async function allocateWithRetry(store, orderId, { attempts, delayMs }) {
  let lastError: unknown;
  for (let i = 1; i <= attempts; i++) {
    try {
      return await store.tryAcquire(orderId); // 슬롯 또는 null이면 재시도 없이 반환
    } catch (e) {
      if (!(e instanceof StoreUnavailableError)) throw e; // 모르는 에러는 재시도하지 않는다
      lastError = e;
      if (i < attempts) await sleep(delayMs);
    }
  }
  throw lastError; // 끝까지 실패하면 라우트에서 503으로 변환
}
```

```ts
// 라우트: 결과는 200, 장애는 503으로 갈라서 응답한다.
if (slot === null) return res.json({ result: "NOT_ALLOCATED" });
return res.json({ result: "ALLOCATED", slot });
// catch (e instanceof StoreUnavailableError) -> res.status(503).json({ result: "STORE_UNAVAILABLE" })
```

### 테스트 팁
`sleep` 간격을 인자로 받게 만들면 테스트에서 0ms로 줄여 빠르게 돌릴 수 있다. 가짜 저장소가 호출 횟수를 세게 하면 '재시도 3회', '자리 없음은 1회'를 검증할 수 있다.

## 🔎 더 공부할 것

- HTTP 503 vs 500 - 의존 서비스 장애와 서버 버그를 어떻게 구분할까
- 지수 백오프(exponential backoff)와 jitter - 고정 간격 재시도의 한계
- Retry-After 헤더 - 503 응답에서 호출자에게 재시도 시점을 알려주는 법
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/503 - 503 상태 코드 공식 설명

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#http-status` `#retry` `#error-handling` `#express` | 2026-10-07 | [▶ 실행해보기](../../example/node-express/dependency-failure-vs-empty-result) |
