<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [layering, http-client, testing, dependency-injection]
date: 2026-11-03
example: example/node-express/outbound-client-separate-from-router
status: auto
-->

# 나가는 HTTP 호출을 라우터에서 클라이언트로 분리

> [!NOTE]
> 외부 서버로 나가는 HTTP 호출을 라우터나 규칙 파일에 섞지 말고 클라이언트 모듈로 빼면, 라우터는 입력 검증과 응답 변환만 맡는다.

> [!WARNING]
> **헷갈린 점** · 클라이언트를 인자로 받게 하면 가짜 클라이언트로 HTTP 없이 규칙을 테스트할 수 있다.

## 📖 자세히

### 왜 분리할까
라우터 파일에 '요청 받기'와 '다른 서버 호출하기'가 같이 있으면, 파일 이름과 실제 역할이 어긋나고(예: 외부가 호출하는 API 파일 안에 외부를 호출하는 함수) 테스트하려면 실제 서버가 필요하다. 나가는 호출은 타임아웃, 주소 조합, 응답 검증이 한곳에 모이는 게 좋다.

```ts
// 클라이언트: 주소/타임아웃/응답 검증만 담당
export function createPaymentClient(baseUrl: string, fetchFn: FetchLike) {
  const base = trimTrailingSlash(baseUrl);
  return {
    async requestCharge(orderId: string, amount: number) {
      const res = await fetchFn(`${base}/charge`, { method: 'POST', /* ... */ });
      if (!res.ok) throw new Error(`charge failed: ${res.status}`);
      return res.json();
    },
  };
}

// 서비스: 클라이언트를 주입받아 규칙만 담당 -> 가짜 클라이언트로 테스트
export async function placeOrder(client: PaymentClient, order: Order) { /* ... */ }
```

### 정리 기준
- 라우터: 입력 검증, 응답 변환
- 규칙 파일: 어디로 보낼지, 무엇을 할지 결정
- 클라이언트: 외부 호출, 타임아웃, 주소 조합
- 둘 이상이 쓰는 작은 함수(주소 끝 `/` 제거 등)는 공통 util로 옮긴다.

### 주의
- 파일을 옮기면서 URL 경로까지 바꾸면 캐시된 옛 프런트 코드가 404를 받을 수 있다. 버전 쿼리나 옛 경로 임시 유지를 고려한다.
- 실패를 로그만 남기고 return하면 사용자는 아무 표시를 못 본다. 실패는 상태 코드로 드러낸다.

## 🔎 더 공부할 것

- 의존성 주입 - 테스트용 가짜 객체를 끼우는 방식
- AbortController - fetch 타임아웃을 거는 표준 방법
- https://nodejs.org/api/test.html - node:test 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#layering` `#http-client` `#testing` `#dependency-injection` | 2026-11-03 | [예제 보기](../../example/node-express/outbound-client-separate-from-router) |
