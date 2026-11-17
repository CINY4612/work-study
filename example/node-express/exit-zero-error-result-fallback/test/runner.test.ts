import { test } from 'node:test';
import assert from 'node:assert/strict';
import { runWithFallback, isSessionMissing, type Runner } from '../src/runner.js';

const missing = { code: 0, stdout: '', stderr: 'No conversation found with session ID: abc' };

test('종료 코드 0이어도 세션 없음 문구면 실패로 본다', () => {
  assert.equal(isSessionMissing(missing), true);
});

test('세션이 있으면 이어서 실행한 결과를 그대로 쓴다', async () => {
  const run: Runner = async () => ({ code: 0, stdout: 'hello', stderr: '' });
  const r = await runWithFallback(run, 'abc');
  assert.deepEqual(r, { text: 'hello', resumed: true });
});

test('세션 없음이면 새 세션으로 한 번 재시도한다', async () => {
  const calls: Array<string | null> = [];
  const run: Runner = async (id) => {
    calls.push(id);
    return id ? missing : { code: 0, stdout: 'fresh', stderr: '' };
  };
  const r = await runWithFallback(run, 'abc');
  assert.deepEqual(r, { text: 'fresh', resumed: false });
  assert.deepEqual(calls, ['abc', null]);
});

test('세션 문제가 아닌 실패는 재시도하지 않고 던진다', async () => {
  let count = 0;
  const run: Runner = async () => {
    count++;
    return { code: 1, stdout: '', stderr: 'boom' };
  };
  await assert.rejects(() => runWithFallback(run, 'abc'), /run failed/);
  assert.equal(count, 1);
});
