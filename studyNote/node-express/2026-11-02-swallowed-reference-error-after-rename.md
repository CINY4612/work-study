<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [node, try-catch, refactoring, reference-error]
date: 2026-11-02
example: example/node-express/swallowed-reference-error-after-rename
status: auto
-->

# 함수 이름을 바꾼 뒤 try/catch에 가려진 ReferenceError

> [!NOTE]
> node --check는 문법만 보므로 지운 함수를 호출하는 줄도 통과하고, 실행 시점에야 ReferenceError가 난다.

> [!WARNING]
> **헷갈린 점** · 그 호출이 try/catch 안이면 에러가 로그로만 남고 흐름이 다른 분기로 흘러 조용히 동작이 바뀐다.

## 📖 자세히

### 왜 이렇게 동작할까
`node --check`(또는 `new Function(...)`)는 코드를 **파싱**만 한다. 없는 이름을 참조하는지는 그 줄을 **실행**할 때 확인되므로 ReferenceError는 런타임에 난다. 호출부가 `try/catch` 안에 있고 catch가 모든 에러를 로그만 남기고 기본값으로 넘기면, 리네임 누락이 "일부 기능이 조용히 다르게 동작"으로 나타난다.

```ts
// 문법은 정상이라 생성은 되지만, 실행하면 ReferenceError
const fn = new Function('return removedFn()');
fn(); // ReferenceError

// 모든 에러를 삼키는 catch: 리네임 누락도 null로 숨겨진다
try { return selectServiceIdsByLoginId(id); } catch (e) { log(e); return null; }

// 프로그래밍 오류(ReferenceError/TypeError)는 다시 던진다
catch (e) {
  if (e instanceof ReferenceError || e instanceof TypeError) throw e;
  log(e); return null;
}
```

### 주의점
- 이름을 바꾼 뒤에는 옛 이름을 전체 검색하고, 호출부를 지나가는 테스트를 한 번 실행한다.
- 부가 조회라서 실패해도 되는 곳이라도, 코드 실수까지 같이 삼키지 않게 에러 종류를 구분한다.
- TypeScript를 쓰면 `tsc` 타입 검사가 이런 누락을 컴파일 단계에서 잡는다. tsx는 타입 검사를 하지 않으므로 별도로 `tsc --noEmit`을 돌려야 한다.

## 🔎 더 공부할 것

- tsc --noEmit - 실행 없이 이름 누락을 잡는 타입 검사
- ESLint no-undef - 정의되지 않은 식별자 정적 검사
- https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ReferenceError - ReferenceError 공식 설명

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#node` `#try-catch` `#refactoring` `#reference-error` | 2026-11-02 | [예제 보기](../../example/node-express/swallowed-reference-error-after-rename) |
