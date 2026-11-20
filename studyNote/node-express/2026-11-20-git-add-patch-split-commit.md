<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [git, add-patch, hunk, commit]
date: 2026-11-20
example: example/node-express/git-add-patch-split-commit
status: auto
-->

# git add -p로 한 파일의 변경을 나눠 커밋

> [!NOTE]
> 두 작업이 한 파일을 같이 고쳤어도 git add -p로 hunk(변경 덩어리) 단위로 골라 담으면 커밋을 작업별로 나눌 수 있다.

> [!WARNING]
> **헷갈린 점** · hunk는 변경 사이가 컨텍스트(기본 3줄)보다 멀 때만 따로 갈라지고, 붙어 있으면 s(split)나 e(edit)가 필요하다.

## 📖 자세히

### 왜 필요한가
`git add 파일`은 파일 전체를 스테이징 영역(index, 다음 커밋에 들어갈 내용을 모아 두는 곳)에 올린다. 그래서 A 작업과 B 작업이 같은 파일을 고쳤다면 파일 단위로는 커밋을 나눌 수 없다. `git add -p`(patch 모드)는 diff를 hunk로 쪼개 보여 주고, 하나씩 `y`(담기) / `n`(건너뛰기)로 고르게 해 준다.

### 동작 원리
1. `git add -p`가 작업 트리와 index의 diff를 계산해 hunk로 나눈다.
2. 각 hunk마다 y / n / s / e 등을 묻는다. 고른 hunk만 index에 올라간다.
3. `git commit`은 index 내용만 커밋하므로, 건너뛴 hunk는 작업 트리에 그대로 남는다.
4. 남은 변경을 다시 담아 두 번째 커밋을 만든다.

```text
작업 트리: [변경 A] ... [변경 B]
 add -p : y            n
 index   : [변경 A]
 commit1 : 변경 A만
 남은 diff: [변경 B] -> commit2
```

### 스크립트로 확인하기
```ts
// 질문마다 입력을 파이프로 넣는다: 첫 hunk는 y, 둘째는 n
git(dir, ["add", "-p"], "y\nn\n");
git(dir, ["commit", "-m", "A 작업"]);
// 건너뛴 변경은 작업 트리에 남아 있다
git(dir, ["diff"]).includes("변경 B"); // true
```

### 자주 하는 실수
- 두 변경이 가까우면 hunk가 하나로 합쳐져 나뉘지 않는다. 이때는 `s`로 쪼개거나 `e`로 patch를 직접 편집한다.
- 새 파일(untracked)은 `-p`에 나오지 않는다. `git add -N 파일`로 먼저 의도만 등록한다.
- 나눈 뒤에는 `git diff --cached`로 index에 의도한 것만 있는지 커밋 전에 확인한다.

## 🔎 더 공부할 것

- git add -e - hunk를 손으로 편집해 더 잘게 나누기
- git stash -p - 일부 변경만 잠시 치워 두기
- git reset --soft - 이미 만든 커밋을 풀어 다시 나누기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#git` `#add-patch` `#hunk` `#commit` | 2026-11-20 | [예제 보기](../../example/node-express/git-add-patch-split-commit) |
