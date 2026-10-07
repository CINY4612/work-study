// 저장소(Redis, DB 등) 장애만 이 예외로 표현한다.
export class StoreUnavailableError extends Error {}

export interface SlotStore {
  // 슬롯 이름 반환 = 배정 성공, null = 빈 슬롯 없음(정상 업무 결과), throw = 저장소 장애
  tryAcquire(orderId: string): Promise<string | null>;
}

export interface RetryOptions {
  attempts: number;
  delayMs: number;
}

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export async function allocateWithRetry(
  store: SlotStore,
  orderId: string,
  { attempts, delayMs }: RetryOptions,
): Promise<string | null> {
  let lastError: unknown;
  for (let i = 1; i <= attempts; i++) {
    try {
      // 슬롯이든 null이든 결과가 나오면 재시도 없이 바로 반환한다.
      return await store.tryAcquire(orderId);
    } catch (e) {
      // 일시 장애만 재시도하고, 그 외 에러는 그대로 던진다.
      if (!(e instanceof StoreUnavailableError)) throw e;
      lastError = e;
      if (i < attempts) await sleep(delayMs);
    }
  }
  throw lastError;
}
