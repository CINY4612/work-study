import { test } from 'node:test';
import assert from 'node:assert/strict';
import http from 'node:http';
import type { AddressInfo } from 'node:net';
import { upstream } from '../src/upstream.js';
import { proxyPass } from '../src/proxy.js';

async function start(listener: http.RequestListener) {
  const server = http.createServer(listener);
  await new Promise<void>((r) => server.listen(0, r));
  const base = `http://127.0.0.1:${(server.address() as AddressInfo).port}`;
  return { base, close: () => server.close() };
}

test('앱에 prefix가 붙은 채 직접 도착하면 /info가 404', async () => {
  const s = await start(upstream);
  const res = await fetch(`${s.base}/1/ws/info?t=${Date.now()}`);
  assert.equal(res.status, 404);
  s.close();
});

test('proxy_pass 끝에 /가 있으면 prefix가 떼어져 /info가 200', async () => {
  const s = await start(proxyPass('/1', upstream, true));
  const res = await fetch(`${s.base}/1/ws/info?t=${Date.now()}`);
  assert.equal(res.status, 200);
  assert.equal((await res.json() as { websocket: boolean }).websocket, true);
  s.close();
});

test('proxy_pass 끝에 /가 없으면 prefix가 그대로 가서 404', async () => {
  const s = await start(proxyPass('/1', upstream, false));
  const res = await fetch(`${s.base}/1/ws/info?t=${Date.now()}`);
  assert.equal(res.status, 404);
  assert.equal((await res.json() as { path: string }).path, '/1/ws/info');
  s.close();
});

test('prefix 밖의 경로는 프록시 단계에서 404', async () => {
  const s = await start(proxyPass('/1', upstream, true));
  const res = await fetch(`${s.base}/2/ws/info`);
  assert.equal(res.status, 404);
  s.close();
});
