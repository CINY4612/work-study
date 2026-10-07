import test from 'node:test';
import assert from 'node:assert/strict';
import { GuardedJoiner, NaiveJoiner } from '../src/roomJoiner.js';

test('status 가드는 응답 전 두 번째 호출을 막지 못한다', () => {
  const j = new NaiveJoiner();
  j.join('room-1'); // 소켓 이벤트 경로
  j.join('room-1'); // HTTP 응답 경로 (status는 아직 READY)
  assert.equal(j.joined.length, 2);
});

test('roomId 가드는 먼저 온 호출 하나만 실행한다', () => {
  const j = new GuardedJoiner();
  assert.equal(j.join('room-1'), true);
  assert.equal(j.join('room-1'), false);
  assert.deepEqual(j.joined, ['room-1']);
});

test('reset 후에는 같은 룸에 다시 입장할 수 있다', () => {
  const j = new GuardedJoiner();
  j.join('room-1');
  j.reset();
  assert.equal(j.join('room-1'), true);
});
