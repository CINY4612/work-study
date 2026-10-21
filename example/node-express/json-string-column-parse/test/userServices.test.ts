import { test } from 'node:test';
import assert from 'node:assert/strict';
import { parseServiceIds } from '../src/userServices.js';

test('id를 꺼내 정렬한다', () => {
  assert.deepEqual(parseServiceIds('[{"id":"2"},{"id":"1"}]'), ['1', '2']);
});

test('null, 빈 문자열, 깨진 JSON은 빈 배열', () => {
  assert.deepEqual(parseServiceIds(null), []);
  assert.deepEqual(parseServiceIds(''), []);
  assert.deepEqual(parseServiceIds('bad'), []);
});

test('배열이 아니거나 id가 없는 항목은 무시한다', () => {
  assert.deepEqual(parseServiceIds('{}'), []);
  assert.deepEqual(parseServiceIds('[{"name":"x"},null,{"id":""}]'), []);
});
