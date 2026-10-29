import test from 'node:test';
import assert from 'node:assert/strict';
import { resolveLoginRedirect } from '../src/loginRedirect.js';

const base = {
  userId: 'u1',
  selfId: 'A',
  frontDoorUrl: 'http://front',
};

test('힌트가 사용자와 같으면 배정기를 부르지 않고 그대로 진행', async () => {
  let called = false;
  const url = await resolveLoginRedirect({
    ...base,
    entryHint: 'u1',
    allocate: async () => {
      called = true;
      return { serverId: 'B', baseUrl: 'http://b' };
    },
  });
  assert.equal(url, '/index.html');
  assert.equal(called, false);
});

test('다른 서버가 배정되면 정문 없이 그 서버로 바로 이동', async () => {
  const url = await resolveLoginRedirect({
    ...base,
    allocate: async () => ({ serverId: 'B', baseUrl: 'http://b' }),
  });
  assert.equal(url, 'http://b/main?hint=u1');
});

test('내가 배정되면 이동하지 않는다', async () => {
  const url = await resolveLoginRedirect({
    ...base,
    allocate: async () => ({ serverId: 'A', baseUrl: 'http://a' }),
  });
  assert.equal(url, '/index.html');
});

test('배정기가 실패하거나 느리면 정문으로 폴백', async () => {
  const failed = await resolveLoginRedirect({
    ...base,
    allocate: async () => {
      throw new Error('down');
    },
  });
  assert.equal(failed, 'http://front/main?hint=u1');

  const slow = await resolveLoginRedirect({
    ...base,
    timeoutMs: 20,
    allocate: () => new Promise(() => {}), // 영원히 응답 없음
  });
  assert.equal(slow, 'http://front/main?hint=u1');
});
