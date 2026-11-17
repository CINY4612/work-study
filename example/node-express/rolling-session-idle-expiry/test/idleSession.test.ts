import test from 'node:test';
import assert from 'node:assert/strict';
import { SessionStore } from '../src/idleSession.js';

const MIN = 60_000;

test('rolling이 아니면 계속 요청해도 로그인 시각 기준으로 만료된다', () => {
  let t = 0;
  const s = new SessionStore(30 * MIN, false, () => t);
  s.login('a');
  t = 20 * MIN;
  assert.equal(s.touch('a'), true);
  t = 31 * MIN;
  assert.equal(s.touch('a'), false); // 활동했는데도 만료
});

test('rolling이면 요청이 이어지는 동안 세션이 유지된다', () => {
  let t = 0;
  const s = new SessionStore(30 * MIN, true, () => t);
  s.login('a');
  for (let i = 1; i <= 10; i++) {
    t = i * 20 * MIN; // 20분마다 heartbeat
    assert.equal(s.touch('a'), true);
  }
});

test('rolling이어도 요청이 끊기면(브라우저 종료) 유휴 시간 뒤 만료된다', () => {
  let t = 0;
  const s = new SessionStore(30 * MIN, true, () => t);
  s.login('a');
  t = 10 * MIN;
  s.touch('a');
  t = 41 * MIN; // 마지막 요청 후 31분 무요청
  assert.equal(s.touch('a'), false);
});
