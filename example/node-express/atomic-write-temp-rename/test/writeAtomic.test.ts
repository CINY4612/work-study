import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';
import { writeAtomic } from '../src/writeAtomic.js';

function tempDir(): string {
  return mkdtempSync(path.join(tmpdir(), 'atomic-'));
}

test('새 파일과 덮어쓰기 모두 내용이 저장되고 임시 파일이 남지 않는다', async () => {
  const dir = tempDir();
  const file = path.join(dir, 'a.md');
  await writeAtomic(file, 'v1');
  await writeAtomic(file, 'v2');
  assert.equal(readFileSync(file, 'utf8'), 'v2');
  assert.deepEqual(readdirSync(dir), ['a.md']);
});

test('교체 단계가 실패해도 원본은 그대로이고 임시 파일은 정리된다', async () => {
  const dir = tempDir();
  const file = path.join(dir, 'a.md');
  writeFileSync(file, 'original');
  const failingRename = async () => {
    throw new Error('disk error');
  };
  await assert.rejects(() => writeAtomic(file, 'new', failingRename), /disk error/);
  assert.equal(readFileSync(file, 'utf8'), 'original');
  assert.deepEqual(readdirSync(dir), ['a.md']);
});
