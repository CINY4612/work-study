import type { PaymentClient } from './paymentClient.js';

export interface Order {
  id: string;
  amount: number;
}

export interface Result {
  status: number;
  body: { ok: boolean; message?: string };
}

// 라우터가 부르는 규칙 함수. 클라이언트를 인자로 받으므로 HTTP 없이 테스트 가능
export async function placeOrder(client: PaymentClient, order: Order): Promise<Result> {
  if (!order.id || order.amount <= 0) {
    return { status: 400, body: { ok: false, message: 'invalid order' } };
  }
  try {
    await client.requestCharge(order.id, order.amount);
    return { status: 200, body: { ok: true } };
  } catch {
    // 실패를 조용히 삼키지 않고 상태 코드로 드러낸다
    return { status: 502, body: { ok: false, message: 'payment unavailable' } };
  }
}
