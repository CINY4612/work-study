import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, readFile, writeFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { hashOf, saveIfUnchanged } from '../src/save.js';

async function setup(initial: string) {
  const dir = await mkdtemp(join(tmpdir(), 'save-'));
  const file = join(dir, 'note.txt');
  await writeFile(file, initial);
  return { dir, file };
}

test('바뀌지 않았으면 저장된다', async () => {
  const { dir, file } = await setup('v1');
  const r = await saveIfUnchanged(file, hashOf('v1'), 'mine');
  assert.deepEqual(r, { ok: true });
  assert.equal(await readFile(file, 'utf8'), 'mine');
  await rm(dir, { recursive: true });
});

test('외부에서 바뀌었으면 덮어쓰지 않는다', async () => {
  const { dir, file } = await setup('v1');
  const loaded = hashOf('v1');
  await writeFile(file, 'external'); // 다른 프로그램이 수정
  const r = await saveIfUnchanged(file, loaded, 'mine');
  assert.deepEqual(r, { ok: false, reason: 'changed-on-disk' });
  assert.equal(await readFile(file, 'utf8'), 'external');
  await rm(dir, { recursive: true });
});

test('파일이 사라졌으면 missing', async () => {
  const { dir, file } = await setup('v1');
  await rm(file);
  const r = await saveIfUnchanged(file, hashOf('v1'), 'mine');
  assert.deepEqual(r, { ok: false, reason: 'missing' });
  await rm(dir, { recursive: true });
});
