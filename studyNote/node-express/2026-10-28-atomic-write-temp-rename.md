<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [fs, atomic-write, rename, node]
date: 2026-10-28
example: example/node-express/atomic-write-temp-rename
status: auto
-->

# 임시 파일에 쓰고 rename하는 원자적 저장

> [!NOTE]
> 같은 폴더의 임시 파일에 먼저 쓰고 rename으로 교체하면 저장 도중 실패해도 원본이 깨지지 않는다.

> [!WARNING]
> **헷갈린 점** · writeFile은 대상 파일을 먼저 비우고 쓰기 때문에, 중간에 끊기면 빈 파일이나 반쪽 파일이 남는다.

## 📖 자세히

### 왜 이렇게 동작할까
`writeFile(file)`은 기존 파일을 열면서 내용을 비운 뒤 새 내용을 쓴다. 쓰는 도중 프로세스가 죽거나 디스크가 가득 차면 원본도 새 내용도 아닌 파일이 남는다.

`rename`은 같은 파일 시스템 안에서는 보통 한 번에 교체되는 동작이다(POSIX는 원자적이고, Windows의 Node도 기존 파일을 덮어쓴다). 그래서 "임시 파일에 완성본을 쓰고 → rename으로 바꿔치기" 순서를 쓰면 다른 쪽에서 보는 파일은 항상 이전 완성본이거나 새 완성본이다.

- 임시 파일은 반드시 **같은 폴더**에 만든다. 다른 드라이브/마운트로 rename하면 복사 후 삭제가 되어 원자성이 사라진다.
- 실패하면 임시 파일을 지우고 에러를 다시 던진다.

```ts
export async function writeAtomic(file: string, data: string, rename = fsRename) {
  const tmp = path.join(path.dirname(file), `.${path.basename(file)}.${process.pid}.tmp`);
  try {
    await writeFile(tmp, data); // 1. 임시 파일에 전부 쓴다
    await rename(tmp, file);    // 2. 한 번에 교체한다
  } catch (e) {
    await rm(tmp, { force: true }); // 실패 시 임시 파일 정리
    throw e;
  }
}
```

rename 함수를 인자로 받게 만들면, 테스트에서 일부러 실패시켜 원본이 보존되는지 확인할 수 있다.

## 🔎 더 공부할 것

- fsync - rename 전에 디스크에 내려쓰는 것까지 보장하려면 필요한 이유
- 같은 파일에 동시에 저장할 때 - 임시 파일 이름이 겹치지 않게 하는 방법
- https://nodejs.org/api/fs.html - fs.rename, fs.rm 동작

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#fs` `#atomic-write` `#rename` `#node` | 2026-10-28 | [예제 보기](../../example/node-express/atomic-write-temp-rename) |
