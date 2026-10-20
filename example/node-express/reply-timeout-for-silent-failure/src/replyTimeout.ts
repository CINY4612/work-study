export type Outcome<T> = { status: 'reply'; value: T } | { status: 'timeout' };

export function waitForReply<T>(
  subscribe: (onReply: (v: T) => void) => void,
  timeoutMs: number,
): Promise<Outcome<T>> {
  return new Promise((resolve) => {
    // 시간 안에 응답이 없으면 timeout으로 확정
    const timer = setTimeout(() => resolve({ status: 'timeout' }), timeoutMs);
    subscribe((v) => {
      clearTimeout(timer); // 응답이 오면 타이머 해제
      resolve({ status: 'reply', value: v }); // 이미 확정됐다면 무시됨
    });
  });
}
