<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [npm, audit, package-lock, semver]
date: 2026-11-10
example: example/node-express/npm-audit-fix-within-range-and-lock
status: auto
-->

# npm audit fix가 하는 일과 lock 파일의 역할

> [!NOTE]
> npm audit fix는 모듈 코드를 고치지 않고, package.json 범위 안에서 패치된 버전으로 바꿔 설치할 뿐이다.

> [!WARNING]
> **헷갈린 점** · 그 결과는 package-lock.json에만 남으므로 lock을 커밋하지 않으면 다른 서버에서 재현되지 않는다.

## 📖 자세히

### 왜 package.json은 그대로일까
`^1.20.3`은 "1.x 중 1.20.3 이상"을 허용한다. audit fix는 이미 허용된 범위 안에서 패치된 최신 버전(예: 1.20.9)을 골라 설치하므로 범위 문자열을 바꿀 필요가 없다. 바뀐 정확한 버전은 `package-lock.json`과 `node_modules`에 기록된다. 취약점이 걸린 패키지 대부분은 직접 설치한 게 아니라 하위 의존성이라 애초에 package.json에 적혀 있지 않다.

1. 취약 패키지에 패치 버전이 있는지 확인한다.
2. 그 버전이 허용 범위 안이면 lock의 해당 항목을 교체한다.
3. 범위 밖(메이저 버전 변경)이면 건드리지 않는다. `--force`를 쓰면 올리지만 호환성이 깨질 수 있다.

```ts
// 범위 안에서만 올린다는 개념 (caret, 메이저 > 0 인 경우)
function satisfiesCaret(version: string, range: string): boolean {
  const [maj, min, pat] = version.split('.').map(Number);
  const [rMaj, rMin, rPat] = range.slice(1).split('.').map(Number);
  if (maj !== rMaj) return false; // 메이저가 다르면 범위 밖
  return min > rMin || (min === rMin && pat >= rPat);
}
```

### lock 파일과 npm ci
- lock에는 직접 설치한 패키지뿐 아니라 하위 의존성 전체의 정확한 버전이 들어 있다.
- `npm install`은 lock이 없으면 그 시점의 범위 내 최신을 받아 결과가 매번 달라질 수 있다.
- `npm ci`는 lock에 적힌 버전을 그대로 설치하므로 배포에 알맞다.
- 하위 의존성 하나를 강제로 고정해야 할 때만 `overrides`를 쓴다. 상위 패키지와 안 맞으면 런타임에서 깨질 수 있어 남용하지 않는다.

### 주의
`package-lock.json`이 `.gitignore`에 들어 있으면 audit fix 결과가 git에 남지 않는다.

## 🔎 더 공부할 것

- npm ci - lock 기준 재현 가능한 설치
- package.json overrides - 하위 의존성 버전 강제
- npm audit fix --force - 메이저 업그레이드의 위험

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#npm` `#audit` `#package-lock` `#semver` | 2026-11-10 | [예제 보기](../../example/node-express/npm-audit-fix-within-range-and-lock) |
