import test from 'node:test';
import assert from 'node:assert/strict';
import { render, safeTemplate, unsafeTemplate } from '../src/render.js';

test('넘기지 않은 변수를 그냥 참조하면 ReferenceError', () => {
  assert.throws(() => render(unsafeTemplate), /notSupport is not defined/);
});

test('locals로 읽으면 넘기지 않아도 일반 화면', () => {
  assert.equal(render(safeTemplate), 'normal');
});

test('값을 넘기면 두 방식 모두 동작', () => {
  assert.equal(render(unsafeTemplate, { notSupport: true }), 'unsupported');
  assert.equal(render(safeTemplate, { notSupport: true }), 'unsupported');
});
