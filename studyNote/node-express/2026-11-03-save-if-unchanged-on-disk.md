<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [file, lost-update, hash, optimistic-check]
date: 2026-11-03
example: example/node-express/save-if-unchanged-on-disk
status: auto
-->

# 저장 직전에 디스크를 다시 읽어 덮어쓰기 막기

> [!NOTE]
> 파일을 읽을 때의 해시를 기억해 두고, 저장 직전에 디스크를 다시 읽어 해시가 같을 때만 쓰면 외부 변경을 덮어쓰지 않는다.

> [!WARNING]
> **헷갈린 점** · 다르면 쓰지 않고 사용자에게 알린다. 확인과 쓰기 사이의 짧은 틈은 남으니 완전한 잠금은 아니다.

## 📖 자세히

### 왜 필요할까
내가 파일을 연 뒤 다른 프로그램이 같은 파일을 고치면, 그냥 저장할 때 그 변경이 사라진다(lost update, 갱신 손실). 읽을 때의 상태를 기억해 두고 저장 직전에 비교하면 이를 감지할 수 있다. DB의 낙관적 락(버전 비교)과 같은 발상이다.

```ts
export async function saveIfUnchanged(path: string, loadedHash: string, content: string) {
  const current = await readFile(path, 'utf8').catch(() => null);
  if (current === null) return { ok: false, reason: 'missing' } as const;
  if (hashOf(current) !== loadedHash) {
    return { ok: false, reason: 'changed-on-disk' } as const; // 덮어쓰지 않음
  }
  await writeFile(path, content);
  return { ok: true } as const;
}
```

### 주의
- 수정 시각(mtime)은 해상도가 거칠어 같은 시각에 바뀌면 놓칠 수 있다. 내용 해시가 더 확실하다.
- 비교와 쓰기 사이에 다른 프로세스가 끼어들 수 있다. 엄격해야 하면 파일 락이나 임시 파일 후 rename을 함께 쓴다.
- 내가 방금 쓴 내용은 새 해시로 갱신해야 다음 저장이 막히지 않는다.

## 🔎 더 공부할 것

- 낙관적 락 - DB에서의 같은 패턴(version 컬럼)
- fs.watch와 폴링 - 외부 변경을 미리 감지하는 방법
- 임시 파일 후 rename - 쓰기 자체를 원자적으로 만들기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#file` `#lost-update` `#hash` `#optimistic-check` | 2026-11-03 | [예제 보기](../../example/node-express/save-if-unchanged-on-disk) |
