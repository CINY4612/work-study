import { test } from 'node:test';
import assert from 'node:assert/strict';
import { Browser, FakeStorage, saveJson, loadJson } from '../src/webStorage.js';

test('값은 문자열로 저장되고 없는 키는 null', () => {
  const s = new FakeStorage();
  s.setItem('n', 1);
  assert.equal(s.getItem('n'), '1');
  assert.equal(s.getItem('none'), null);
});

test('탭을 닫으면 session만 사라지고 local은 남는다', () => {
  const b = new Browser();
  b.session.setItem('agentId', 'user01');
  b.local.setItem('collapsed', 'true');
  b.closeTab();
  assert.equal(b.session.getItem('agentId'), null);
  assert.equal(b.local.getItem('collapsed'), 'true');
});

test('객체는 JSON으로 왕복한다', () => {
  const s = new FakeStorage();
  saveJson(s, 'cfg', { tab: 'rooms' });
  assert.deepEqual(loadJson(s, 'cfg', {}), { tab: 'rooms' });
});

test('깨진 JSON은 기본값으로 대체한다', () => {
  const s = new FakeStorage();
  s.setItem('cfg', '{broken');
  assert.deepEqual(loadJson(s, 'cfg', { tab: 'default' }), { tab: 'default' });
});
