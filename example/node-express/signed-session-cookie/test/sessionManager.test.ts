import test from 'node:test';
import assert from 'node:assert/strict';
import { SessionManager } from '../src/sessionManager.js';

const toCookieHeader = (setCookie: string) => setCookie.split(';')[0];

test('빈 세션은 쿠키를 내보내지 않는다', () => {
  const m = new SessionManager('secret');
  assert.equal(m.commit(m.open()), null);
});

test('로그인 후 쿠키로 세션이 복원된다', () => {
  const m = new SessionManager('secret');
  const s = m.regenerate(m.open());
  s.data.userId = 'u1';
  const setCookie = m.commit(s);
  assert.ok(setCookie);
  const restored = m.open(toCookieHeader(setCookie));
  assert.equal(restored.data.userId, 'u1');
  assert.equal(restored.isNew, false);
});

test('sid를 바꾼 쿠키는 새 빈 세션이 된다', () => {
  const m = new SessionManager('secret');
  const s = m.regenerate(m.open());
  s.data.userId = 'u1';
  const setCookie = m.commit(s)!;
  const tampered = toCookieHeader(setCookie).replace('sid=', 'sid=x');
  const restored = m.open(tampered);
  assert.equal(restored.isNew, true);
  assert.equal(restored.data.userId, undefined);
});
