import { test } from 'node:test';
import assert from 'node:assert/strict';
import { parseParts, resolveSavePath } from '../src/multipartOrder.js';

// FormData를 실제 multipart 본문 문자열로 직렬화한다 (append 순서가 그대로 유지됨)
async function serialize(fd: FormData) {
  const res = new Response(fd);
  return { contentType: res.headers.get('content-type')!, body: await res.text() };
}

test('필드를 파일보다 먼저 보내면 필드로 경로가 정해진다', async () => {
  const fd = new FormData();
  fd.append('callId', 'c1');
  fd.append('fileName', 'a.txt');
  fd.append('file', new Blob(['hi']), 'raw.txt');
  const { contentType, body } = await serialize(fd);
  assert.equal(resolveSavePath(parseParts(body, contentType)), 'uploads/c1/a.txt');
});

test('파일을 먼저 보내면 필드를 아직 몰라 기본 경로에 저장된다', async () => {
  const fd = new FormData();
  fd.append('file', new Blob(['hi']), 'raw.txt');
  fd.append('callId', 'c1');
  fd.append('fileName', 'a.txt');
  const { contentType, body } = await serialize(fd);
  assert.equal(resolveSavePath(parseParts(body, contentType)), 'uploads/unknown/raw.txt');
});
