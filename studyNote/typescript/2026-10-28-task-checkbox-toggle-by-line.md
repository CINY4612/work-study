<!-- meta (read by scripts, hidden on GitHub)
category: typescript
tags: [markdown, checkbox, source-map, string]
date: 2026-10-28
example: example/node-express/task-checkbox-toggle-by-line
status: auto
-->

# 소스 줄 번호로 마크다운 체크박스 토글

> [!NOTE]
> 렌더된 N번째 체크박스를 원문에서 찾으려면 '몇 번째'가 아니라 파서가 준 소스 줄 번호를 기준으로 그 줄만 바꾼다.

> [!WARNING]
> **헷갈린 점** · 줄 구분자(CRLF/LF)를 보존해야 저장 시 파일 전체가 변경으로 잡히지 않는다.

## 📖 자세히

### 왜 줄 번호인가
화면의 체크박스를 클릭해 원문에 반영할 때 '문서 안 N번째 `[ ]`'를 정규식으로 찾으면 코드 블록 안의 `[ ]`와 섞여 엉뚱한 곳이 바뀐다. markdown-it 같은 파서는 토큰에 소스 줄 범위(`token.map[0]`)를 담아 주므로, 렌더 시 이를 `data-line`으로 심어 두고 클릭 때 그 줄만 수정하면 정확하다.

### 핵심 코드
```ts
const TASK = /^(\s*(?:[-*+]|\d+[.)])\s+\[)([ xX])(\]\s)/;

export function toggleTaskLine(source: string, line: number): string {
  // 캡처 그룹 split: [내용, 구분자, 내용, 구분자, ...] 라서 줄 구분자가 그대로 남는다
  const parts = source.split(/(\r?\n)/);
  const text = parts[line * 2];
  if (text === undefined || !TASK.test(text)) return source;
  parts[line * 2] = text.replace(TASK, (_m, head, mark, tail) => head + (mark === ' ' ? 'x' : ' ') + tail);
  return parts.join('');
}
```

### 주의점
- 줄 번호는 반드시 파서가 준 값을 써야 한다. 직접 센 번호는 코드 펜스 같은 구조 때문에 어긋날 수 있다.
- 사용자가 편집 중인 내용이 있는 상태에서 체크만 바꿔 바로 저장하면 다른 미저장 편집이 같이 저장된다. 편집 중이면 '수정됨' 표시만 하는 편이 안전하다.

## 🔎 더 공부할 것

- markdown-it token.map - 토큰별 소스 줄 범위 얻기
- String.prototype.split 캡처 그룹 - 구분자를 보존하며 나누기
- 낙관적 저장 충돌 - 외부에서 파일이 바뀐 경우 덮어쓰기 방지

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `typescript` | `#markdown` `#checkbox` `#source-map` `#string` | 2026-10-28 | [예제 보기](../../example/node-express/task-checkbox-toggle-by-line) |
