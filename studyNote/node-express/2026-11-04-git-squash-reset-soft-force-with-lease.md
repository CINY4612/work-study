<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [git, squash, reset, force-with-lease]
date: 2026-11-04
example: example/node-express/git-squash-reset-soft-force-with-lease
status: auto
-->

# reset --soft로 커밋 합치고 force-with-lease로 올리기

> [!NOTE]
> 기준 커밋까지 `git reset --soft <base>` 후 다시 commit하면 여러 커밋이 하나로 합쳐지고 파일 내용은 그대로 남는다.

> [!WARNING]
> **헷갈린 점** · 이미 push한 커밋이면 히스토리가 바뀌므로 `--force-with-lease`로 올려야 남의 push를 덮어쓰지 않는다.

## 📖 자세히

### 왜 reset --soft일까
`git reset`은 브랜치가 가리키는 커밋만 옮깁니다. `--soft`는 작업 폴더와 스테이징(index)을 건드리지 않아서, 되돌린 커밋들의 변경 내용이 전부 staged 상태로 남습니다. 거기서 `commit`을 한 번 하면 그 변경이 커밋 하나가 됩니다.

```bash
git branch backup/work          # 실수 대비 백업
git reset --soft <base>         # base 이후 커밋을 풀어 staged로 남김
git commit -m "Feat: 합친 메시지"
git diff backup/work HEAD       # 출력이 없으면 내용이 같다
```

### force push는 왜 조심해야 할까
합친 커밋은 해시가 완전히 새로 생겨서, 이미 push한 브랜치에는 일반 push가 거절됩니다. `--force`는 원격이 무엇이든 덮어쓰지만, `--force-with-lease`는 내가 마지막으로 본 원격 상태와 같을 때만 덮어씁니다. 그사이 다른 사람이 push했다면 멈춥니다.

```bash
git push --force-with-lease origin my-branch
```

### 비교
| 방법 | 특징 |
|---|---|
| `reset --soft` + commit | 간단하고 빠르다. 메시지를 새로 쓴다 |
| `rebase -i` + squash | 기존 메시지를 이어 붙일 수 있다. 에디터가 필요하다 |

### 주의점
- 다른 사람이 그 브랜치를 받아 작업 중이면 force push 후 히스토리가 꼬입니다. 미리 알려야 합니다.
- 아직 push하지 않은 새 브랜치에서 합치면 force push가 필요 없습니다.
- 합칠 때 기준 브랜치(`origin/main`인지 `master`인지)를 확인하세요. 기준이 뒤처져 있으면 관계없는 커밋까지 딸려 옵니다.

## 🔎 더 공부할 것

- git reflog - reset으로 잃은 것처럼 보이는 커밋을 되찾는 방법
- git rebase -i squash/fixup - 기존 메시지를 살려 합치기
- git commit --amend - 대표 메시지만 바꿀 때

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#git` `#squash` `#reset` `#force-with-lease` | 2026-11-04 | [예제 보기](../../example/node-express/git-squash-reset-soft-force-with-lease) |
