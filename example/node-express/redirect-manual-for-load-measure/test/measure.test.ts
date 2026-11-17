import { test } from 'node:test';
import assert from 'node:assert/strict';
import type { AddressInfo } from 'node:net';
import { createApp } from '../src/app.js';
import { countByLocation } from '../src/measure.js';

async function withServer(fn: (base: string) => Promise<void>) {
  const server = createApp().listen(0);
  const { port } = server.address() as AddressInfo;
  try {
    await fn(`http://127.0.0.1:${port}`);
  } finally {
    server.close();
  }
}

test('manual 모드는 302와 Location 분포를 볼 수 있다', async () => {
  await withServer(async (base) => {
    const res = await fetch(base + '/enter', { redirect: 'manual' });
    assert.equal(res.status, 302);
    await res.text();
    const counts = await countByLocation(base, '/enter', 10);
    assert.equal(counts['/node/1/home'], 5);
    assert.equal(counts['/node/2/home'], 5);
  });
});

test('follow 모드는 최종 목적지의 200만 보인다', async () => {
  await withServer(async (base) => {
    const res = await fetch(base + '/enter'); // 기본값 follow
    assert.equal(res.status, 200);
    assert.equal(res.redirected, true);
    assert.equal(res.headers.get('location'), null);
    await res.text();
  });
});
