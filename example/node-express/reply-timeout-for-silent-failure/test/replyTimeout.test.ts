import { test } from 'node:test';
import assert from 'node:assert/strict';
import { waitForReply } from '../src/replyTimeout.js';

test('응답이 오면 reply', async () => {
  const outcome = await waitForReply<number>((onReply) => {
    setTimeout(() => onReply(200), 5);
  }, 100);
  assert.deepEqual(outcome, { status: 'reply', value: 200 });
});

test('응답이 없으면 timeout', async () => {
  const outcome = await waitForReply<number>(() => {
    // 상대가 아무것도 보내지 않는 상황
  }, 20);
  assert.deepEqual(outcome, { status: 'timeout' });
});

test('timeout 뒤에 늦게 온 응답은 결과를 바꾸지 못한다', async () => {
  let late: (v: number) => void = () => {};
  const promise = waitForReply<number>((onReply) => {
    late = onReply;
  }, 20);
  const outcome = await promise;
  late(200); // 이미 확정된 뒤
  assert.deepEqual(outcome, { status: 'timeout' });
  assert.deepEqual(await promise, { status: 'timeout' });
});
