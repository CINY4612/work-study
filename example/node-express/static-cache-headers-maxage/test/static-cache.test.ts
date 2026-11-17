import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { get } from 'node:http';
import type { AddressInfo } from 'node:net';
import type { Express } from 'express';
import { createStaticApp } from '../src/app.js';

// 임시 폴더에 파일을 만들고, 포트 0(빈 포트 자동 선택)으로 서버를 띄운다.
function setup(maxAge?: string) {
  const dir = mkdtempSync(join(tmpdir(), 'static-'));
  writeFileSync(join(dir, 'page.html'), '<h1>hello</h1>');
  const app: Express = createStaticApp(dir, maxAge);
  const server = app.listen(0);
  const url = `http://127.0.0.1:${(server.address() as AddressInfo).port}/page.html`;
  return { url, close: () => server.close() };
}

test('기본값은 max-age=0 이고 ETag가 붙는다', async () => {
  const { url, close } = setup();
  try {
    const res = await fetch(url);
    assert.equal(res.status, 200);
    assert.equal(res.headers.get('cache-control'), 'public, max-age=0');
    assert.ok(res.headers.get('etag'));
  } finally {
    close();
  }
});

// fetch(undici)는 If-None-Match를 직접 넣으면 Fetch 표준대로 Cache-Control: no-cache를 덧붙인다.
// express는 no-cache 요청을 '신선하지 않음'으로 보고 200을 주므로, 조건부 요청은 헤더를 그대로 보내는 node:http로 보낸다.
function statusWith(url: string, headers: Record<string, string>): Promise<number> {
  return new Promise((resolve, reject) => {
    get(url, { headers }, (res) => {
      res.resume();
      resolve(res.statusCode ?? 0);
    }).on('error', reject);
  });
}

test('ETag를 If-None-Match로 보내면 본문 없이 304', async () => {
  const { url, close } = setup();
  try {
    const first = await fetch(url);
    const etag = first.headers.get('etag')!;
    assert.equal(await statusWith(url, { 'If-None-Match': etag }), 304);
  } finally {
    close();
  }
});

test('maxAge 옵션을 주면 그 시간만큼 캐시를 허용한다', async () => {
  const { url, close } = setup('1h');
  try {
    const res = await fetch(url);
    assert.equal(res.headers.get('cache-control'), 'public, max-age=3600');
  } finally {
    close();
  }
});
