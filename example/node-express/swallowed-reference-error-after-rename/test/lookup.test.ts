import { test } from 'node:test';
import assert from 'node:assert/strict';
import { resolveSwallowAll, resolveStrict } from '../src/lookup.js';

test('문법 검사는 통과하지만 실행하면 ReferenceError', () => {
  const fn = new Function('return removedFn()'); // 파싱 성공
  assert.throws(() => fn(), ReferenceError); // 실행 시점에 실패
});

test('모든 에러를 삼키면 null로 조용히 넘어간다', () => {
  const logs: string[] = [];
  assert.equal(resolveSwallowAll('u1', (m) => logs.push(m)), null);
  assert.match(logs[0], /ReferenceError/);
});

test('엄격한 버전은 코드 실수를 밖으로 던진다', () => {
  assert.throws(() => resolveStrict('u1', () => {}), ReferenceError);
});
