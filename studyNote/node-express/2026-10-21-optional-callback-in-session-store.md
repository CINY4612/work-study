<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [express-session, session-store, callback, TypeError]
date: 2026-10-21
example: example/node-express/optional-callback-in-session-store
status: auto
-->

# 세션 스토어의 콜백은 선택값으로 받기

> [!NOTE]
> express-session은 destroy()를 콜백 없이 부를 수 있어서, 스토어가 callback을 무조건 호출하면 TypeError가 난다.

> [!WARNING]
> **헷갈린 점** · 세션 row는 이미 지워졌는데도 에러 로그가 남아 '삭제 실패'처럼 보이는 점이 헷갈린다.

## 📖 자세히

### 왜 이런 에러가 날까
`req.session.destroy()`처럼 콜백 없이 부르면 express-session은 `undefined`를 그대로 `store.destroy(sid, undefined)`에 넘긴다. 스토어 구현이 `callback(err)`를 무조건 호출하면 `callback is not a function`이 된다.

1. 호출하는 쪽이 콜백을 생략한다 (문법상 허용되는 사용법).
2. 스토어의 DB 삭제는 이미 실행되고 커밋됐다.
3. 그 뒤 완료 콜백을 부르는 순간 TypeError가 발생한다.
4. DB 래퍼가 이 예외를 try/catch로 잡으면 '쿼리 실패' 로그로 남고, 이미 닫힌 커넥션을 다시 닫으려 하는 2차 문제도 생길 수 있다.

### 고치는 곳
호출하는 쪽을 하나씩 고치기보다, 스토어에서 콜백을 선택값으로 처리하면 어디서 호출해도 안전하다.

```ts
destroy(sid: string, callback?: (err?: Error | null) => void): void {
  deleteRow(this.sessions, sid, (err) => callback && callback(err)); // 콜백이 있을 때만 호출
}
```

`set`, `touch`도 같은 방식으로 처리한다. 삭제가 끝난 뒤 redirect하고 싶으면 호출하는 쪽에서 `req.session.destroy(() => res.redirect(url))`처럼 콜백을 넘기면 된다.

## 🔎 더 공부할 것

- express-session Store 인터페이스 - get/set/destroy/touch 콜백 규약을 확인
- unhandled promise rejection - catch 안에서 다시 예외가 나면 어떻게 되는지
- Promise 기반 스토어 - 콜백 대신 async/await로 감싸는 방법

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#express-session` `#session-store` `#callback` `#TypeError` | 2026-10-21 | [예제 보기](../../example/node-express/optional-callback-in-session-store) |
