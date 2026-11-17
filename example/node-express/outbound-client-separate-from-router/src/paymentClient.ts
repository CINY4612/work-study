// 나가는 HTTP 호출만 담당하는 클라이언트
export type FetchLike = (
  url: string,
  init: { method: string; headers: Record<string, string>; body: string },
) => Promise<{ ok: boolean; status: number; json(): Promise<unknown> }>;

export interface PaymentClient {
  requestCharge(orderId: string, amount: number): Promise<unknown>;
}

// 주소 끝의 / 를 지워서 `${base}/charge` 가 //charge 가 되지 않게 한다
export function trimTrailingSlash(url: string): string {
  return url.replace(/\/+$/, '');
}

export function createPaymentClient(baseUrl: string, fetchFn: FetchLike): PaymentClient {
  const base = trimTrailingSlash(baseUrl);
  return {
    async requestCharge(orderId, amount) {
      const res = await fetchFn(`${base}/charge`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ orderId, amount }),
      });
      if (!res.ok) throw new Error(`charge failed: ${res.status}`); // 응답 검증은 여기서
      return res.json();
    },
  };
}
