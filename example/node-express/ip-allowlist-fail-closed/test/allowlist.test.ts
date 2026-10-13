import { test } from 'node:test';
import assert from 'node:assert/strict';
import type { AddressInfo } from 'node:net';
import { createApp, type AppConfig } from '../src/app.js';
import { normalizeIp } from '../src/allowlist.js';

// 임시 포트로 서버를 띄워 실제 HTTP 호출 결과의 상태 코드를 돌려준다.
async function call(config: AppConfig): Promise<number> {
  const server = createApp(config).listen(0);
  try {
    const { port } = server.address() as AddressInfo;
    const res = await fetch(`http://127.0.0.1:${port}/internal/books/lookup`, { method: 'POST' });
    return res.status;
  } finally {
    server.close();
  }
}

test('normalizeIp는 IPv4-mapped 접두사를 뗀다', () => {
  assert.equal(normalizeIp('::ffff:10.0.0.1'), '10.0.0.1');
  assert.equal(normalizeIp('10.0.0.1'), '10.0.0.1');
});

test('허용 목록에 있는 IP는 200', async () => {
  assert.equal(await call({ allowedIps: ['127.0.0.1'] }), 200);
});

test('목록에 없는 IP는 403', async () => {
  assert.equal(await call({ allowedIps: ['192.0.2.10'] }), 403);
});

test('설정이 없으면 전부 403', async () => {
  assert.equal(await call({}), 403);
});

test('빈 목록도 전부 403', async () => {
  assert.equal(await call({ allowedIps: [] }), 403);
});
