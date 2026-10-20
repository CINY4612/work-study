<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [timeout, promise, error-handling, websocket]
date: 2026-10-20
example: example/node-express/reply-timeout-for-silent-failure
status: auto
-->

# 응답이 오지 않는 실패는 타임아웃으로 잡기

> [!NOTE]
> 오류 응답 핸들러(code != 200)는 상대가 오류를 '보내줄 때'만 동작한다. 응답 자체가 안 오는 실패는 타이머로 따로 잡아야 한다.

> [!WARNING]
> **헷갈린 점** · 타임아웃 뒤에 늦게 온 응답이 상태를 다시 바꾸지 않도록 Promise를 한 번만 확정시킨다.

## 📖 자세히

### 왜 이렇게 동작할까
메시지 기반 통신(웹소켓 등)에서 요청을 보내고 응답 콜백을 등록해 두면, 상대가 연결되지 않았거나 중간에서 요청이 버려진 경우 콜백은 영원히 호출되지 않는다. 오류 처리 코드는 콜백 안에 있으므로 함께 실행되지 않는다. 그래서 '응답 대기 시간'을 정하고, 그 시간이 지나면 같은 실패 처리를 하도록 해야 한다.

```ts
export type Outcome<T> = { status: 'reply'; value: T } | { status: 'timeout' };

export function waitForReply<T>(
  subscribe: (onReply: (v: T) => void) => void,
  timeoutMs: number,
): Promise<Outcome<T>> {
  return new Promise((resolve) => {
    // 시간 안에 응답이 없으면 timeout으로 확정
    const timer = setTimeout(() => resolve({ status: 'timeout' }), timeoutMs);
    subscribe((v) => {
      clearTimeout(timer); // 응답이 오면 타이머 해제
      resolve({ status: 'reply', value: v }); // 이미 확정됐다면 무시됨
    });
  });
}
```

- Promise는 처음 resolve된 값만 유지하므로, 타임아웃 후 늦은 응답이 와도 결과가 바뀌지 않는다.
- 호출자는 `timeout`과 오류 응답을 같은 실패 경로로 보내면 된다.
- 타임아웃 값은 정상 응답 지연보다 충분히 크게 잡는다. 너무 짧으면 정상 요청이 실패로 처리된다.

## 🔎 더 공부할 것

- AbortController - fetch 요청을 타임아웃으로 취소하기
- Promise.race - 타임아웃을 만드는 다른 방법
- 멱등성 - 타임아웃 뒤 재시도해도 안전하게 만들기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#timeout` `#promise` `#error-handling` `#websocket` | 2026-10-20 | [예제 보기](../../example/node-express/reply-timeout-for-silent-failure) |
