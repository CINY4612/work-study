import test from 'node:test';
import assert from 'node:assert/strict';
import { satisfiesCaret, pickPatched } from '../src/range.js';

test('같은 메이저의 높은 버전은 범위 안이다', () => {
  assert.equal(satisfiesCaret('1.20.9', '^1.20.3'), true);
});

test('메이저가 올라가면 범위 밖이다', () => {
  assert.equal(satisfiesCaret('2.0.0', '^1.20.3'), false);
});

test('범위 안에서 가장 높은 버전을 고르므로 package.json 문자열은 바꿀 필요가 없다', () => {
  const picked = pickPatched('^1.20.3', ['1.20.3', '1.20.9', '2.0.0']);
  assert.equal(picked, '1.20.9');
});
