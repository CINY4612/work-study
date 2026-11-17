<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [ejs, xss, ssr, script-injection]
date: 2026-11-04
example: example/node-express/server-data-injection-script-json
status: auto
-->

# 서버 데이터를 script에 심을 때 JSON 이스케이프

> [!NOTE]
> 서버 값을 `var g = <%- json %>`처럼 raw로 넣을 때는 `<`, `>`, `&`를 유니코드 이스케이프로 바꿔야 `</script>` 삽입을 막는다.

> [!WARNING]
> **헷갈린 점** · `<%= %>`는 HTML 이스케이프라 JS 객체에 쓰면 깨지고, `<%- %>`는 이스케이프가 없어서 XSS 위험이 있다.

## 📖 자세히

### 왜 이 패턴을 쓸까
세션이나 설정처럼 서버에만 있는 값을 `public/`의 정적 JS는 직접 읽을 수 없습니다. 그래서 서버가 HTML을 만들 때(SSR, 서버 사이드 렌더링) `<script>` 안에 초기 데이터를 전역 변수로 박아 줍니다. 페이지가 열리자마자 값을 쓸 수 있어 API를 한 번 더 부르지 않아도 됩니다.

### EJS 태그 차이
| 태그 | 동작 |
|---|---|
| `<% 코드 %>` | 코드만 실행, 출력 없음 |
| `<%= 값 %>` | HTML 이스케이프해서 출력 |
| `<%- 값 %>` | 그대로(raw) 출력 |

문자열은 따옴표 안에 `<%=`를 쓰고, 객체/배열은 JSON 문법 그대로 들어가야 해서 `<%-`를 씁니다.

### 왜 위험할까
`JSON.stringify`는 `<`를 바꾸지 않습니다. 값에 `</script><script>alert(1)</script>`가 있으면 HTML 파서가 첫 `</script>`에서 스크립트를 닫아 버려서 뒤 내용이 실행됩니다. 해결은 `<`를 `\u003c`처럼 바꾸는 것입니다. JSON 문자열 안에서 `\u003c`는 JS가 같은 값으로 읽으므로 데이터는 변하지 않습니다.

```ts
export function toScriptJson(value: unknown): string {
  return JSON.stringify(value)
    .replace(/</g, '\\u003c')
    .replace(/>/g, '\\u003e')
    .replace(/&/g, '\\u0026')
    .replace(/\u2028/g, '\\u2028') // 옛 JS에서 줄바꿈으로 취급되는 문자
    .replace(/\u2029/g, '\\u2029');
}
```

## 🔎 더 공부할 것

- XSS와 CSP - 인라인 script를 제한하는 방어 방법
- HTML 이스케이프 규칙 - 속성값, script 안, URL마다 다르다
- EJS 공식 문서 - 태그 종류와 include

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#ejs` `#xss` `#ssr` `#script-injection` | 2026-11-04 | [예제 보기](../../example/node-express/server-data-injection-script-json) |
