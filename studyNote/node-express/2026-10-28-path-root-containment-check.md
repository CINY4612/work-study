<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [path-traversal, realpath, security, node]
date: 2026-10-28
example: example/node-express/path-root-containment-check
status: auto
-->

# 허용 루트 밖 경로를 막는 경로 검사

> [!NOTE]
> 사용자가 준 경로는 realpath로 실제 위치를 구한 뒤 path.relative로 루트 안인지 확인한다. 문자열 startsWith 비교는 쓰지 않는다.

> [!WARNING]
> **헷갈린 점** · '/data/notes-evil'은 '/data/notes'로 시작하므로 startsWith는 통과시키고, ../나 심볼릭 링크도 문자열 비교로는 못 잡는다.

## 📖 자세히

### 왜 문자열 비교로는 부족할까
경로 탈출(path traversal)은 `../`나 절대 경로, 심볼릭 링크로 허용된 폴더 밖 파일에 접근하는 공격이다. 파일 API가 "알아서 폴더 안으로 제한해 주겠지"라고 믿으면 안 된다. 직접 만든 파일 읽기/쓰기 함수는 프레임워크의 접근 범위 검사를 거치지 않는 경우가 많아서 코드에서 직접 검사해야 한다.

1. `path.resolve(root, userPath)`로 `..`를 계산해 절대 경로로 만든다. userPath가 절대 경로면 root는 무시된다.
2. `realpathSync`로 심볼릭 링크를 풀어 실제 위치를 구한다. 링크가 루트 밖을 가리키는 경우를 잡기 위해서다.
3. `path.relative(realRoot, real)`이 `..`로 시작하면 루트 밖이다. 문자열 `startsWith(root)`는 `notes-evil`도 통과시키므로 쓰지 않는다.
4. 아직 없는 파일(새로 저장)은 realpath가 실패하므로, 존재하는 부모까지만 풀고 이름을 다시 붙인다.

```ts
export function resolveInside(root: string, userPath: string): string {
  const realRoot = realpathSync(root);
  const real = realOrParent(path.resolve(realRoot, userPath));
  const rel = path.relative(realRoot, real);
  // '..' 자체이거나 '../' 로 시작하거나(다른 드라이브처럼) 절대 경로면 루트 밖
  const escapes = rel === '..' || rel.startsWith('..' + path.sep) || path.isAbsolute(rel);
  if (escapes) throw new Error('허용된 루트 밖의 경로입니다');
  return real;
}
```

### 잘못된 예
```ts
// '/data/notes-evil/x.md'.startsWith('/data/notes') === true  -> 통과해 버림
if (!path.resolve(root, p).startsWith(root)) throw new Error('denied');
```
루트 뒤에 구분자를 붙여 비교하더라도 심볼릭 링크는 막지 못하므로 `relative` + `realpath` 조합이 안전하다.

## 🔎 더 공부할 것

- TOCTOU(검사 시점과 사용 시점 사이의 경쟁) - 검사 직후 링크가 바뀌는 경우를 어떻게 줄이는지
- path.win32 vs path.posix - 구분자와 대소문자 차이가 검사에 미치는 영향
- express.static의 dotfiles 옵션 - 정적 서빙이 경로 탈출을 막는 방식
- https://nodejs.org/api/path.html - path.relative / path.resolve 동작

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#path-traversal` `#realpath` `#security` `#node` | 2026-10-28 | [예제 보기](../../example/node-express/path-root-containment-check) |
