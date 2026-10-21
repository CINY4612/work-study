import { test } from 'node:test';
import assert from 'node:assert/strict';
import { NaiveStore, SafeStore } from '../src/store.js';

test('콜백 없이 부르면 NaiveStore는 TypeError (row는 이미 지워진 뒤)', () => {
  const store = new NaiveStore();
  store.sessions.set('a', 'data');
  assert.throws(() => store.destroy('a'), TypeError);
  assert.equal(store.sessions.has('a'), false);
});

test('콜백 없이 불러도 SafeStore는 에러 없이 삭제', () => {
  const store = new SafeStore();
  store.sessions.set('a', 'data');
  assert.doesNotThrow(() => store.destroy('a'));
  assert.equal(store.sessions.has('a'), false);
});

test('콜백을 주면 SafeStore가 삭제 후 null로 호출', () => {
  const store = new SafeStore();
  store.sessions.set('a', 'data');
  let result: Error | null | undefined = undefined;
  store.destroy('a', (err) => { result = err; });
  assert.equal(result, null);
});
