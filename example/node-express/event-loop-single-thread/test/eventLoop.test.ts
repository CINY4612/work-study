import { test } from 'node:test';
import assert from 'node:assert/strict';
import { runOrder, timerDelayWhileBlocked, interleave } from '../src/eventLoop.js';

test('동기 -> 마이크로태스크 -> 타이머 순서로 실행된다', async () => {
  assert.deepEqual(await runOrder(), ['sync', 'microtask', 'timeout']);
});

test('동기 작업이 스레드를 점유하면 10ms 타이머도 그만큼 늦게 실행된다', async () => {
  const elapsed = await timerDelayWhileBlocked(50);
  assert.ok(elapsed >= 50, `elapsed=${elapsed}`);
});

test('await 지점에서 스레드가 비어 두 작업이 번갈아 실행된다', async () => {
  const log = await interleave();
  assert.deepEqual(log, ['A0', 'B0', 'A1', 'B1']);
});
