import test from 'node:test';
import assert from 'node:assert/strict';
import { generateSid, sidLength, collisionProbability } from '../src/sid.js';

test('기본 24바이트 sid는 32자다', () => {
  assert.equal(generateSid().length, 32);
  assert.equal(sidLength(24), 32);
});

test('genid로 15바이트를 쓰면 20자가 된다', () => {
  assert.equal(generateSid(15).length, 20);
  assert.equal(sidLength(15), 20);
});

test('sid는 URL에 안전한 문자만 쓴다', () => {
  assert.match(generateSid(), /^[A-Za-z0-9_-]+$/);
});

test('만 개를 만들어도 겹치지 않는다', () => {
  const set = new Set(Array.from({ length: 10000 }, () => generateSid()));
  assert.equal(set.size, 10000);
});

test('세션 10억 개 기준 충돌 확률: 192비트는 극히 작고 120비트도 작다', () => {
  assert.ok(collisionProbability(1e9, 192) < 1e-30);
  assert.ok(collisionProbability(1e9, 120) < 1e-17);
});
