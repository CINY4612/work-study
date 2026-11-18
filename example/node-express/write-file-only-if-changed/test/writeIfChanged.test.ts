import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, readFile, stat, utimes } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { writeIfChanged } from '../src/writeIfChanged.js';

async function tempFile(): Promise<string> {
  const dir = await mkdtemp(join(tmpdir(), 'write-if-changed-'));
  return join(dir, 'out.txt');
}

test('파일이 없으면 새로 만든다', async () => {
  const file = await tempFile();
  assert.equal(await writeIfChanged(file, 'v1'), true);
  assert.equal(await readFile(file, 'utf8'), 'v1');
});

test('내용이 같으면 쓰지 않아 mtime이 유지된다', async () => {
  const file = await tempFile();
  await writeIfChanged(file, 'same');
  // mtime을 과거로 돌려 두고, 다시 써도 바뀌지 않는지 확인한다
  const past = new Date('2020-01-01T00:00:00Z');
  await utimes(file, past, past);

  assert.equal(await writeIfChanged(file, 'same'), false);
  assert.equal((await stat(file)).mtime.getTime(), past.getTime());
});

test('내용이 바뀌면 쓰고 mtime이 갱신된다', async () => {
  const file = await tempFile();
  await writeIfChanged(file, 'old');
  const past = new Date('2020-01-01T00:00:00Z');
  await utimes(file, past, past);

  assert.equal(await writeIfChanged(file, 'new'), true);
  assert.equal(await readFile(file, 'utf8'), 'new');
  assert.ok((await stat(file)).mtime.getTime() > past.getTime());
});
