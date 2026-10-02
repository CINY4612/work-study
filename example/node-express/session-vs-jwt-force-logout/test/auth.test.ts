import { test } from 'node:test';
import assert from 'node:assert/strict';
import { SessionAuth, VersionedJwtAuth, signJwt, verifyJwt } from '../src/auth.js';

const NOW = 1_000_000;

test('세션: 강제 로그아웃하면 다음 요청부터 바로 무효', () => {
  const auth = new SessionAuth();
  const sid = auth.login('u1');
  assert.equal(auth.isValid(sid), true);
  auth.forceLogout('u1');
  assert.equal(auth.isValid(sid), false);
});

test('순수 JWT: 서버가 할 수 있는 게 없어 만료 전까지 계속 유효', () => {
  const token = signJwt({ sub: 'u1', exp: NOW + 60 }, 'secret');
  // 사용자를 로그아웃시켜도 서버에는 지울 상태가 없다
  assert.notEqual(verifyJwt(token, 'secret', NOW + 10), null);
  assert.equal(verifyJwt(token, 'secret', NOW + 61), null); // 만료만이 유일한 종료 수단
});

test('JWT: 서명이 다르면 거부', () => {
  const token = signJwt({ sub: 'u1', exp: NOW + 60 }, 'secret');
  assert.equal(verifyJwt(token, 'other', NOW), null);
});

test('버전 JWT: 버전을 올리면 기존 토큰이 무효, 새 토큰은 유효', () => {
  const auth = new VersionedJwtAuth('secret');
  const old = auth.issue('u1', NOW);
  assert.equal(auth.check(old, NOW), true);
  auth.forceLogout('u1');
  assert.equal(auth.check(old, NOW), false);
  assert.equal(auth.check(auth.issue('u1', NOW), NOW), true);
});
