<!-- meta (read by scripts, hidden on GitHub)
category: javascript
tags: [automation, jsonl, batch]
date: 2026-09-30
example: example/javascript/batch-candidates
status: auto
-->

# 수집은 무료로, 비싼 호출은 배치 1회로 분리하는 법

> [!NOTE]
> 이벤트마다 JSONL 큐에 메타데이터만 쌓고, 배치에서 후보를 골라 비싼 호출(LLM 등)은 한 번만 한다.

> [!WARNING]
> **헷갈린 점** · 고를 게 없으면 호출 자체를 건너뛰고, 처리한 id는 done으로 남겨야 큐가 무한히 커지거나 같은 항목을 두 번 처리하지 않는다.

## 📖 자세히

JSONL(JSON Lines)은 한 줄에 JSON 객체 하나를 쓰는 형식이다. 파일 끝에 한 줄만 덧붙이면 되니 이벤트가 생길 때마다 싸게 기록할 수 있고, 한 줄이 깨져도 그 줄만 버리면 된다.
비용이 드는 작업(LLM 호출, 외부 API)은 이벤트마다 하지 않고 정해진 시각의 배치에서 모아서 한 번에 처리한다. 호출 횟수가 이벤트 수가 아니라 배치 수에 비례하므로 비용을 예측할 수 있다.
후보 선택 규칙은 "고정 항목 우선 → 기준 이상만 → 최신순 → 상한"이다. 상한이 있어야 한 번에 너무 많이 몰려도 호출 크기가 일정하다.

```js
export function pickCandidates({ pinned, collected, done = new Set(), minChanges = 2, max = 6 }) {
  const pinnedIds = new Set(pinned.map((t) => t.id));
  const auto = collected
    .filter((t) => !done.has(t.id) && !pinnedIds.has(t.id) && t.changes >= minChanges) // 처리됨·중복·기준 미달 제외
    .sort((a, b) => b.ts.localeCompare(a.ts)); // 최신순
  const room = Math.max(0, max - pinned.length); // 고정 항목이 먼저 자리를 차지
  return [...pinned, ...auto.slice(0, room)];
}

export async function runBatch(input, expensiveCall) {
  const picked = pickCandidates(input);
  if (picked.length === 0) return { called: false, picked }; // 고를 게 없으면 호출 안 함
  return { called: true, picked, result: await expensiveCall(picked) }; // 배치당 1회
}
```

## 🔎 더 공부할 것

- 멱등성(idempotency) - 같은 배치를 다시 돌려도 결과가 중복되지 않게 만드는 법
- 작업 큐 / 메시지 큐 - 파일 큐를 넘어설 때 쓰는 구조 (예: BullMQ)
- cron 스케줄링 - 배치를 정해진 시각에 실행하는 방법

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `javascript` | `#automation` `#jsonl` `#batch` | 2026-09-30 | [▶ 실행해보기](../../example/javascript/batch-candidates) |
