<!-- meta (read by scripts, hidden on GitHub)
category: javascript
tags: [sessionStorage, localStorage, browser, web-storage]
date: 2026-10-30
example: example/node-express/web-storage-session-vs-local
status: auto
-->

# sessionStorage와 localStorage 개념 정리

> [!NOTE]
> sessionStorage는 탭을 닫으면 사라지고 localStorage는 직접 지울 때까지 남는다. 입력값 복원과 UI 상태 저장에 쓴다.

> [!WARNING]
> **헷갈린 점** · 값은 항상 문자열로 저장되고, 없는 키는 undefined가 아니라 null이다. 객체는 JSON으로 변환해야 한다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
웹 스토리지는 브라우저가 제공하는 키-값 저장소다(쿠키와 달리 요청마다 서버로 전송되지 않는다). 새로고침해도 입력값이나 접힘 상태를 유지하고 싶을 때 쓴다.

### 동작 원리
1. `setItem(key, value)`는 value를 문자열로 바꿔 저장한다.
2. `getItem(key)`는 저장된 문자열을 돌려주고, 키가 없으면 `null`을 돌려준다.
3. sessionStorage는 탭(브라우징 컨텍스트) 단위이고 탭을 닫으면 지워진다. 새로고침에는 살아남는다.
4. localStorage는 같은 출처(origin: 프로토콜+호스트+포트)라면 탭이 달라도 공유되고 브라우저를 닫아도 남는다.

```text
새로고침      : session O / local O
탭 닫기       : session X / local O
다른 탭(같은 출처): session 분리 / local 공유
```

### 구성 요소/용어 정리
| 구분 | sessionStorage | localStorage |
|---|---|---|
| 수명 | 탭이 닫힐 때까지 | 직접 지울 때까지 |
| 공유 범위 | 해당 탭 | 같은 출처의 모든 탭 |
| 어울리는 용도 | 작성 중인 입력값 | 접기/펼치기, 마지막 선택 탭 |

### 코드로 보기
```ts
// 객체는 문자열로만 저장되므로 JSON 변환이 필요하다
export function saveJson(s: Pick<Storage, 'setItem'>, key: string, value: unknown): void {
  s.setItem(key, JSON.stringify(value));
}

// 없는 키(null)와 깨진 JSON 모두 기본값으로 대체한다
export function loadJson<T>(s: Pick<Storage, 'getItem'>, key: string, fallback: T): T {
  const raw = s.getItem(key);
  if (raw === null) return fallback;
  try { return JSON.parse(raw) as T; } catch { return fallback; }
}
```

### 비교
| 항목 | 쿠키 | 웹 스토리지 |
|---|---|---|
| 서버 전송 | 요청마다 자동 | 자동 전송 안 함 |
| 용량 | 약 4KB | 보통 약 5MB |
| 용도 | 세션 식별 | 클라이언트 UI 상태 |

### 자주 하는 실수와 주의점
- `setItem('n', 1)` 후 `getItem('n')`은 숫자 1이 아니라 문자열 `'1'`이다.
- `JSON.parse(null)`은 null을 돌려주고, 깨진 문자열은 예외를 던진다. 읽는 쪽에서 방어한다.
- 토큰이나 비밀번호 같은 민감 정보는 스크립트(XSS)로 읽을 수 있으므로 저장하지 않는다.

### 세션에서 나온 질문 정리
- Q. 새로고침해도 입력한 ID를 남기고 브라우저를 닫으면 지우려면? A. sessionStorage에 저장한다.
- Q. 패널 접힘 상태를 다음 방문에도 유지하려면? A. localStorage에 저장한다.

## 🔎 더 공부할 것

- Cookie vs Web Storage - 서버 전송 여부와 보안 속성(HttpOnly) 차이
- storage 이벤트 - 다른 탭에서 localStorage가 바뀔 때 감지
- https://developer.mozilla.org/docs/Web/API/Web_Storage_API - 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `javascript` | `#sessionStorage` `#localStorage` `#browser` `#web-storage` | 2026-10-30 | [예제 보기](../../example/node-express/web-storage-session-vs-local) |
