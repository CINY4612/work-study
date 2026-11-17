<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [child-process, exit-code, session-resume, retry, error-handling]
date: 2026-11-04
example: example/node-express/exit-zero-error-result-fallback
status: auto
-->

# 종료 코드 0이어도 실패일 수 있는 자식 프로세스

> [!NOTE]
> CLI를 자식 프로세스로 실행할 때 종료 코드 0은 성공의 증거가 아니다. 출력 안의 오류 문구와 빈 결과까지 봐야 한다.

> [!WARNING]
> **헷갈린 점** · 세션 ID로 이어서 실행(resume)하는 CLI는 그 세션이 없는 환경에서 오류를 내고도 0으로 끝날 수 있어, 빈 응답으로 둔갑한다.

## 📖 자세히

### 왜 이런 일이 생길까
세션 기록은 보통 계정이나 프로필마다 따로 있는 설정 폴더에 저장된다. 저장해 둔 세션 ID를 다른 프로필로 실행할 때 넘기면 그 폴더에는 세션 파일이 없다. 그러면 CLI는 `No conversation found with session ID: ...` 같은 문구를 출력하고 끝난다. 이때 종료 코드가 0일 수 있다. 이 CLI가 "요청은 처리했고 결과가 오류였다"는 식으로 종료하기 때문이다.

호출하는 쪽이 `code !== 0`일 때만 재시도하도록 짜 두었다면 이 실패는 정상 종료로 분류된다. 결과 텍스트가 비어 있으니 화면에는 "(응답 없음)"만 남는다.

### 판정을 세 단계로 나눈다
1. 종료 코드가 0이 아니면 실패다.
2. 종료 코드가 0이어도 출력에 알려진 오류 문구가 있으면 실패다. 원인별로 이름을 붙여 둔다(예: 세션 없음).
3. 결과가 비어 있어도 실패다. 빈 문자열을 성공으로 넘기지 않는다.

### 코드로 보기
```ts
// 세션 없음은 종료 코드와 상관없이 따로 식별한다
export function isSessionMissing(r: RunResult): boolean {
  return /No conversation found/i.test(r.stderr + r.stdout);
}

export async function runWithFallback(run: Runner, sessionId: string | null) {
  if (sessionId) {
    const first = await run(sessionId);
    const ok = first.code === 0 && !isSessionMissing(first) && first.stdout.trim() !== '';
    if (ok) return { text: first.stdout, resumed: true };
    // 세션이 없는 경우에만 새 세션으로 한 번 더 시도한다 (다른 실패는 그대로 노출)
    if (!isSessionMissing(first)) throw new Error('run failed: ' + first.stderr);
  }
  const fresh = await run(null);
  if (fresh.code !== 0 || fresh.stdout.trim() === '') throw new Error('run failed');
  return { text: fresh.stdout, resumed: false };
}
```

### 주의점
- 재시도는 원인이 확실한 경우(세션 없음)에만 한다. 모든 실패를 새 세션으로 재시도하면 진짜 오류가 가려진다.
- 재시도는 한 번으로 제한한다. 무한 재시도는 비용과 장애를 키운다.
- 새 세션으로 시작하면 이전 대화 맥락은 사라진다. 호출한 쪽에 resumed 여부를 돌려줘서 안내할 수 있게 한다.

## 🔎 더 공부할 것

- child_process.spawn - 종료 코드, signal, stdout/stderr 스트림을 따로 다루는 법 (https://nodejs.org/api/child_process.html)
- 멱등한 재시도 - 재시도해도 부작용이 두 번 생기지 않게 설계하기
- 빈 결과 vs 실패 - 의존 서비스 응답을 구분해서 다루는 기준

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#child-process` `#exit-code` `#session-resume` `#retry` `#error-handling` | 2026-11-04 | [예제 보기](../../example/node-express/exit-zero-error-result-fallback) |
