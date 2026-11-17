import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createPaymentClient, type FetchLike } from '../src/paymentClient.js';
import { placeOrder } from '../src/orderService.js';

test('가짜 클라이언트로 성공 경로를 HTTP 없이 테스트한다', async () => {
  const client = { requestCharge: async () => ({ paid: true }) };
  const r = await placeOrder(client, { id: 'o1', amount: 1000 });
  assert.equal(r.status, 200);
});

test('클라이언트가 실패하면 502로 드러난다', async () => {
  const client = {
    requestCharge: async () => {
      throw new Error('boom');
    },
  };
  const r = await placeOrder(client, { id: 'o1', amount: 1000 });
  assert.equal(r.status, 502);
});

test('잘못된 입력은 클라이언트를 부르기 전에 400', async () => {
  let called = false;
  const client = {
    requestCharge: async () => {
      called = true;
      return {};
    },
  };
  const r = await placeOrder(client, { id: '', amount: 10 });
  assert.equal(r.status, 400);
  assert.equal(called, false);
});

test('클라이언트는 주소 끝 슬래시를 정리하고 비정상 응답을 에러로 바꾼다', async () => {
  let usedUrl = '';
  const fetchFn: FetchLike = async (url) => {
    usedUrl = url;
    return { ok: false, status: 500, json: async () => ({}) };
  };
  const client = createPaymentClient('http://pay.example/', fetchFn);
  await assert.rejects(() => client.requestCharge('o1', 10), /charge failed: 500/);
  assert.equal(usedUrl, 'http://pay.example/charge');
});
