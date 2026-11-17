import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import type { AddressInfo } from 'node:net';
import { createApp } from '../src/app.js';

async function hit(staticFirst: boolean, path: string) {
  const dir = mkdtempSync(join(tmpdir(), 'static-'));
  writeFileSync(join(dir, 'app.css'), 'body{}');
  const { app, stats } = createApp({ staticDir: dir, staticFirst });
  const server = app.listen(0);
  try {
    const { port } = server.address() as AddressInfo;
    const res = await fetch(`http://127.0.0.1:${port}${path}`);
    await res.text();
    return { status: res.status, lookups: stats.sessionLookups };
  } finally {
    server.closeAllConnections();
    server.close();
  }
}

test('static을 앞에 두면 정적 파일 요청은 세션을 조회하지 않는다', async () => {
  const r = await hit(true, '/app.css');
  assert.equal(r.status, 200);
  assert.equal(r.lookups, 0);
});

test('session을 앞에 두면 정적 파일 요청도 세션을 조회한다', async () => {
  const r = await hit(false, '/app.css');
  assert.equal(r.status, 200);
  assert.equal(r.lookups, 1);
});

test('static을 앞에 둬도 API 요청은 세션을 거친다', async () => {
  const r = await hit(true, '/api/me');
  assert.equal(r.status, 200);
  assert.equal(r.lookups, 1);
});
