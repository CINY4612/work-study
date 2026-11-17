// 자식 프로세스 실행 결과 (종료 코드, 표준 출력, 표준 오류)
export interface RunResult {
  code: number;
  stdout: string;
  stderr: string;
}

// sessionId가 있으면 이어서 실행, null이면 새 세션으로 실행하는 함수
export type Runner = (sessionId: string | null) => Promise<RunResult>;

// 종료 코드가 0이어도 출력에 이 문구가 있으면 세션이 없는 것이다
export function isSessionMissing(r: RunResult): boolean {
  return /No conversation found/i.test(r.stderr + r.stdout);
}

function isSuccess(r: RunResult): boolean {
  // 종료 코드, 오류 문구, 빈 결과를 모두 확인한다
  return r.code === 0 && !isSessionMissing(r) && r.stdout.trim() !== '';
}

export async function runWithFallback(
  run: Runner,
  sessionId: string | null,
): Promise<{ text: string; resumed: boolean }> {
  if (sessionId) {
    const first = await run(sessionId);
    if (isSuccess(first)) return { text: first.stdout, resumed: true };
    // 원인이 세션 없음일 때만 새 세션으로 재시도한다. 다른 실패는 숨기지 않는다.
    if (!isSessionMissing(first)) {
      throw new Error('run failed: ' + first.stderr);
    }
  }
  const fresh = await run(null); // 재시도는 딱 한 번
  if (!isSuccess(fresh)) throw new Error('run failed after fallback');
  return { text: fresh.stdout, resumed: false };
}
