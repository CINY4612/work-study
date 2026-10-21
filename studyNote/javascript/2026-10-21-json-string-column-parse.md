<!-- meta (read by scripts, hidden on GitHub)
category: javascript
tags: [json, parsing, database, defensive]
date: 2026-10-21
example: example/node-express/json-string-column-parse
status: auto
-->

# JSON 문자열 컬럼은 읽는 쪽에서 파싱하기

> [!NOTE]
> DB 컬럼을 JSON 타입 대신 VARCHAR에 JSON 문자열로 저장하면 SQL의 JSON 함수 없이 앱에서 JSON.parse로 읽을 수 있다.

> [!WARNING]
> **헷갈린 점** · 파싱 실패를 빈 배열로 삼키면 편하지만, 데이터가 깨진 것도 정상적인 빈 값처럼 보이니 로그를 남기는 편이 안전하다.

## 📖 자세히

### 왜 이렇게 동작할까
JSON 타입 컬럼은 DB마다 문법이 달라서 `JSON_TABLE` 같은 DB 전용 함수에 쿼리가 묶인다. 문자열 컬럼에 JSON 텍스트를 저장하면 쿼리는 값을 그대로 읽기만 하고, 해석은 앱 코드가 맡는다. 쿼리가 단순해지고 DB를 바꿔도 파싱 코드는 그대로 쓸 수 있다.

대신 DB는 형식을 보장해 주지 않는다. 그래서 읽는 쪽에서 null, 빈 문자열, 깨진 JSON, 배열이 아닌 값, 필드가 없는 항목을 모두 고려해야 한다.

```ts
export function parseServiceIds(raw: string | null | undefined): string[] {
  if (!raw) return [];                       // null, 빈 문자열
  let parsed: unknown;
  try {
    parsed = JSON.parse(raw);                // 깨진 JSON은 예외가 난다
  } catch {
    return [];                               // 운영에서는 여기서 warn 로그를 남기는 것이 좋다
  }
  if (!Array.isArray(parsed)) return [];     // '{}' 같은 배열이 아닌 값
  return parsed
    .map((item) => (item as { id?: unknown } | null)?.id)
    .filter((id): id is string => typeof id === 'string' && id !== '')
    .sort();                                 // 기존 ORDER BY와 같은 순서를 앱에서 보장
}
```

### 주의할 점
- 길이는 바이트 기준일 수 있다. Oracle의 `VARCHAR2(n)` 기본 단위는 BYTE라서 UTF-8 한글은 글자당 3바이트로 계산된다. 필요하면 `CHAR` 단위를 쓴다.
- DB에서도 형식을 막고 싶으면 `CHECK (col IS JSON)` 같은 제약을 추가할 수 있다.
- 빈 배열 반환은 "데이터 없음"과 "데이터 깨짐"을 구분하지 못한다. 깨진 경우는 최소한 로그로 남긴다.

## 🔎 더 공부할 것

- JSON.parse - 잘못된 입력에서 던지는 SyntaxError 처리 https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/JSON/parse
- 타입 가드 - unknown 값을 안전하게 좁히는 방법
- CHECK 제약 - DB에서 JSON 형식을 검증하는 방법

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `javascript` | `#json` `#parsing` `#database` `#defensive` | 2026-10-21 | [예제 보기](../../example/node-express/json-string-column-parse) |
