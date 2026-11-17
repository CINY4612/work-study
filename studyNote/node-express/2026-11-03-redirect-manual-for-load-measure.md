<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [redirect, load-test, fetch, k6]
date: 2026-11-03
example: example/node-express/redirect-manual-for-load-measure
status: auto
-->

# 리다이렉트를 따라가지 않고 서버 분배 측정하기

> [!NOTE]
> 302를 주는 진입점을 측정할 때 리다이렉트를 따라가면 최종 목적지까지 재서, 진입 서버의 성능이 아니라 전체 경로를 재게 된다.

> [!WARNING]
> **헷갈린 점** · redirect: 'manual'로 받으면 302 자체와 Location 헤더를 볼 수 있어 노드별 분배 비율도 셀 수 있다.

## 📖 자세히

### 왜 이렇게 동작할까
브라우저와 `fetch`는 기본값이 `redirect: 'follow'`라서 302를 받으면 Location으로 자동 재요청한다. 부하 테스트 도구(k6 등)도 기본은 따라간다. 그러면 지연 시간에 뒤쪽 서버의 처리 시간이 섞이고, 어느 노드로 보냈는지는 응답에 남지 않는다.

`manual`로 바꾸면 첫 응답(302)에서 멈추므로 다음 두 가지를 얻는다.
1. 진입 서버만의 응답 시간 (뒤쪽 서버 부하가 섞이지 않음)
2. `Location` 헤더 값 → 어느 목적지로 분배했는지 집계

```ts
// 리다이렉트를 따라가지 않고 Location만 세기
const res = await fetch(base + '/enter', { redirect: 'manual' });
const loc = res.headers.get('location') ?? 'none';
counts[loc] = (counts[loc] ?? 0) + 1;
await res.text(); // 본문을 소비해야 연결이 정리된다
```

### 주의
- k6에서는 옵션 `redirects: 0` 또는 요청별 `redirects: 0`으로 같은 효과를 낸다.
- 분배가 한쪽으로 쏠리는지 보려면 응답 시간만이 아니라 Location 분포도 같이 봐야 한다.

## 🔎 더 공부할 것

- k6 redirects 옵션 - 도구별로 리다이렉트 제어 방법이 다르다
- fetch redirect 모드 - follow/manual/error 차이 https://developer.mozilla.org/en-US/docs/Web/API/Request/redirect
- 부하 테스트 중 헬스체크 오판 - 부하가 상태 판정을 왜곡할 수 있다

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#redirect` `#load-test` `#fetch` `#k6` | 2026-11-03 | [예제 보기](../../example/node-express/redirect-manual-for-load-measure) |
