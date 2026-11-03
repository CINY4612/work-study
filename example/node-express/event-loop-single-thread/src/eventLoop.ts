// 동기 -> 마이크로태스크(Promise) -> 타이머 순서를 기록해서 돌려준다
export async function runOrder(): Promise<string[]> {
  const log: string[] = [];
  setTimeout(() => log.push('timeout'), 0); // 태스크 큐
  Promise.resolve().then(() => log.push('microtask')); // 마이크로태스크 큐
  log.push('sync'); // 지금 바로
  await sleep(5); // 스레드를 비워서 대기열이 처리되게 한다
  return log;
}

export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

// 스레드를 점유하는 동기 작업 (실무에서는 피해야 하는 코드)
export function blockFor(ms: number): void {
  const start = Date.now();
  while (Date.now() - start < ms) {
    // busy wait
  }
}

// 10ms 뒤 실행을 예약하고 blockMs 동안 스레드를 점유 -> 실제로 콜백이 돈 시점(경과 ms)
export function timerDelayWhileBlocked(blockMs: number): Promise<number> {
  const start = Date.now();
  return new Promise((resolve) => {
    setTimeout(() => resolve(Date.now() - start), 10);
    blockFor(blockMs);
  });
}

// await 지점마다 스레드가 비므로 두 작업이 번갈아 진행된다
export async function interleave(): Promise<string[]> {
  const log: string[] = [];
  const task = async (name: string) => {
    for (let i = 0; i < 2; i++) {
      log.push(`${name}${i}`);
      await sleep(5);
    }
  };
  await Promise.all([task('A'), task('B')]);
  return log;
}
