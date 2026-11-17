<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [git, cherry-pick, merge, squash, author]
date: 2026-10-29
example: example/node-express/git-cherry-pick-vs-merge
status: auto
-->

# cherry-pick과 merge로 커밋 옮기기 개념 정리

> [!NOTE]
> cherry-pick은 커밋을 새 해시로 복사하고 작성자는 유지하며, merge는 해시를 그대로 두고 merge 커밋을 하나 더 만든다.

> [!WARNING]
> **헷갈린 점** · staging에 남은 변경이 있으면 충돌이 아니어도 cherry-pick이 거부되고, merge --squash는 작성자를 합치는 사람으로 바꾼다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
다른 브랜치에 쌓인 커밋 몇 개만 내 브랜치로 가져오고 싶을 때 쓰는 도구가 `cherry-pick`과 `merge`다. 둘 다 "코드 취합"이 목적이지만 히스토리에 남는 모양이 다르다. 그래서 작성자 보존, 메시지 수정, 충돌 해결 횟수 같은 요구에 따라 골라야 한다.

### 동작 원리
1. **cherry-pick**: 지정한 커밋의 변경분(부모와의 diff)을 현재 브랜치 끝에 다시 적용해 새 커밋을 만든다. 내용은 같지만 부모가 달라 해시는 새로 생긴다.
2. **merge**: 두 브랜치의 공통 조상, 내 쪽, 상대 쪽 3개 버전으로 3-way merge를 한다. 상대 커밋은 해시 그대로 이력에 들어오고, 부모가 둘인 merge 커밋이 생긴다.
3. **merge --squash**: 변경 내용만 staging에 올리고 커밋은 만들지 않는다. 직접 `git commit`을 하면 그 커밋은 한 개이고 작성자는 커밋하는 사람이다.

```text
cherry-pick:  main: A - M - A' - B'        (A', B'는 새 해시, 작성자 유지)
merge --no-ff: main: A - M ------- X       (X는 merge 커밋)
                      \\- a - b -/          (a, b는 원래 해시 그대로)
squash:       main: A - M - S              (S 하나, 작성자 = 커밋한 사람)
```

### 구성 요소/용어 정리
| 용어 | 의미 |
|---|---|
| author | 변경을 처음 작성한 사람. cherry-pick과 `commit -c`는 이 값을 유지한다 |
| committer | 커밋을 만든 사람. cherry-pick하면 이쪽은 현재 사용자로 바뀐다 |
| 공통 조상 | merge가 기준으로 삼는 두 브랜치의 갈라진 지점 |
| `A..B` | A에는 없고 B에는 있는 커밋 범위 |
| `CHERRY_PICK_HEAD` | 충돌로 멈춘 cherry-pick이 "진행 중"임을 표시하는 파일 |

### 코드로 보기
아래 헬퍼는 임시 저장소에서 git 명령을 실행하고, 작성자를 환경변수로 바꿔 가며 커밋을 만든다 (예제 `gitLab.ts`).

```ts
export function git(dir: string, args: string[], author = 'alice'): string {
  return execFileSync('git', args, {
    cwd: dir,
    encoding: 'utf8',
    env: {
      ...process.env,
      GIT_AUTHOR_NAME: author, // author는 환경변수로 지정
      GIT_AUTHOR_EMAIL: `${author}@example.com`,
      GIT_COMMITTER_NAME: 'committer',
      GIT_COMMITTER_EMAIL: 'committer@example.com',
    },
  }).trim();
}
```

실제 작업에서 쓰는 명령 흐름은 이렇다.

```bash
# 3개 커밋만 가져오기. -e 를 붙이면 커밋마다 메시지 편집기가 열린다
git cherry-pick -e base..feature
# 충돌 해결 후 이어가기 / 전부 취소
git cherry-pick --continue
git cherry-pick --abort
# cherry-pick 진행 상태가 사라졌다면 원래 작성자를 가져와 커밋
git commit -c <원래커밋>
```

### 비교
| | cherry-pick | merge --no-ff | merge --squash |
|---|---|---|---|
| 들어오는 커밋 | 개수만큼 복사(새 해시) | 원래 해시 + merge 커밋 1개 | 커밋 1개(직접 만듦) |
| 작성자 | 유지 | 유지 | 커밋한 사람으로 바뀜 |
| 충돌 해결 | 커밋마다 따로 | 한 번에 | 한 번에 |
| 메시지 수정 | `-e`로 즉시 | merge 전에 상대 브랜치에서 reword | 커밋할 때 자유롭게 |
| 히스토리 | 일직선 | 갈라졌다 합쳐짐 | 일직선 |

### 자주 하는 실수와 주의점
- **staging에 남은 변경**: 이전 작업에서 `git add`만 하고 남은 변경이 있으면, 파일이 겹치지 않아도 cherry-pick이 시작되지 않는다. `git restore --staged <파일>`로 비운 뒤 다시 한다. 범위(`A..B`)로 cherry-pick했다면 거부된 뒤에도 진행 상태가 남아 "already in progress"가 나므로, 먼저 `git cherry-pick --quit`으로 진행 상태만 지운다.
- **충돌 해결 중 `git commit`**: `CHERRY_PICK_HEAD`가 있으면 `--continue`가 맞다. 그 파일이 사라졌다면 평범한 `git commit`은 작성자가 나로 바뀌므로 `git commit -c <원래커밋>`을 쓴다.
- **기준 브랜치 착각**: 어느 브랜치와 비교해 "딸려오는 커밋"을 세는지에 따라 결과가 다르다. 공통 조상을 `git merge-base A B`로 확인한다.
- **충돌 해결 때 한쪽 기능 삭제**: 충돌 파일만 고치다가 상대 쪽의 `case` 분기 같은 코드가 같이 지워질 수 있다. 자동 merge 결과와 비교해 확인한다.
- **공유한 브랜치의 메시지 변경**: 이미 push한 커밋을 reword하면 `--force-with-lease`가 필요하다. 아직 push 전이면 자유롭게 고친다.

### 세션에서 나온 질문 정리
**Q. 커밋 여러 개를 하나로 합치면서 작성자를 여러 명 남길 수 있나?**
A. 작성자별로 그룹을 나눠 커밋 하나씩 만들면 된다. `GIT_AUTHOR_NAME` 같은 환경변수나 `--author`로 지정한다. 한 커밋은 작성자가 한 명뿐이다.

**Q. cherry-pick 말고 merge로 해도 되나?**
A. 공통 조상이 원하는 커밋이라면 들어오는 건 그 커밋들뿐이라 괜찮다. 대신 메시지를 바꿀 거라면 merge 전에 상대 브랜치에서 먼저 바꾼다.

**Q. squash merge는 왜 목적에 안 맞았나?**
A. 커밋이 하나로 합쳐지면서 작성자가 전부 커밋하는 사람으로 바뀌기 때문이다.

**Q. 충돌 파일이 없는데 cherry-pick이 거부됐다. 왜?**
A. staging area가 HEAD와 달라서다. git은 안전을 위해 이 상태에서 cherry-pick을 시작하지 않는다.

## 🔎 더 공부할 것

- git rebase -i (reword/squash) - 아직 push 안 한 커밋의 메시지와 구성을 정리할 때
- git merge-base - 두 브랜치의 공통 조상을 확인해 딸려오는 커밋 범위 파악
- git reflog - 잘못된 reset/cherry-pick 이후 되돌릴 지점 찾기
- git rerere - 반복되는 충돌 해결을 기억시키기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#git` `#cherry-pick` `#merge` `#squash` `#author` | 2026-10-29 | [예제 보기](../../example/node-express/git-cherry-pick-vs-merge) |
