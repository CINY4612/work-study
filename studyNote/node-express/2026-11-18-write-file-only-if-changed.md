<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [fs, build, mtime, idempotent]
date: 2026-11-18
example: example/node-express/write-file-only-if-changed
status: auto
-->

# 내용이 같으면 생성 파일을 다시 쓰지 않기

> [!NOTE]
> 빌드 스크립트가 같은 내용을 다시 쓰면 mtime(수정 시각)이 바뀌어, 변경을 감지하는 다운스트림이 불필요하게 다시 일어난다.

> [!WARNING]
> **헷갈린 점** · 쓰기 전에 기존 내용과 비교해서 바뀐 경우에만 쓰면 재실행해도 결과가 같고(멱등), 파생 캐시도 지켜진다.

## 📖 자세히

### 왜 문제가 될까
파일 감시기, 번들러, 에셋 임포터 같은 도구는 보통 **mtime이나 해시로 '바뀌었는지'를 판단**한다. 생성 스크립트가 내용이 같아도 매번 덮어쓰면 mtime이 갱신되어 도구가 '바뀌었다'고 오해하고, 파생 산출물을 지우거나 다시 만든다. 심하면 파생물이 사라진 채 원본만 그대로여서 재생성이 일어나지 않는 상태가 될 수 있다.

해결은 단순하다. 쓰기 전에 기존 파일을 읽어 바이트가 같으면 아무것도 하지 않는다.

```ts
import { readFile, writeFile } from 'node:fs/promises';

export async function writeIfChanged(path: string, next: Buffer | string): Promise<boolean> {
  const data = typeof next === 'string' ? Buffer.from(next) : next;
  try {
    const current = await readFile(path);
    if (current.equals(data)) return false; // 같으면 쓰지 않는다 (mtime 유지)
  } catch (e) {
    if ((e as NodeJS.ErrnoException).code !== 'ENOENT') throw e; // 파일 없음만 정상 흐름
  }
  await writeFile(path, data);
  return true;
}
```

### 주의점
- 반환값(`true/false`)으로 '실제로 바뀌었는지'를 알려 주면, 호출 쪽이 바뀐 경우에만 후속 정리(캐시 삭제 등)를 하게 만들 수 있다.
- 큰 파일은 전체를 메모리에 읽으므로 해시 비교로 바꾸는 것을 고려한다.
- 이 기법은 '쓰기 시점에 다른 프로세스가 고치는 경우'를 막는 것이 아니다. 그건 임시 파일 + rename 같은 다른 문제다.

## 🔎 더 공부할 것

- crypto.createHash - 큰 파일은 해시로 비교한다
- fs.watch와 mtime - 도구들이 변경을 감지하는 방식
- 멱등성(idempotency) - 같은 작업을 반복해도 결과가 같게 만드는 설계

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#fs` `#build` `#mtime` `#idempotent` | 2026-11-18 | [예제 보기](../../example/node-express/write-file-only-if-changed) |
