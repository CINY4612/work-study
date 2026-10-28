import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, writeFileSync, symlinkSync, realpathSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';
import { resolveInside } from '../src/safePath.js';

const base = mkdtempSync(path.join(tmpdir(), 'safe-'));
const root = path.join(base, 'notes');
const sibling = path.join(base, 'notes-evil'); // 이름이 root로 시작하는 다른 폴더
mkdirSync(root);
mkdirSync(sibling);
writeFileSync(path.join(root, 'a.md'), 'hi');
writeFileSync(path.join(sibling, 'secret.md'), 'secret');

test('루트 안의 기존 파일과 새 파일은 허용한다', () => {
  const realRoot = realpathSync(root);
  assert.equal(resolveInside(root, 'a.md'), path.join(realRoot, 'a.md'));
  assert.equal(resolveInside(root, 'new.md'), path.join(realRoot, 'new.md'));
});

test('../ 와 절대 경로로 루트 밖에 나가면 막는다', () => {
  assert.throws(() => resolveInside(root, '../notes-evil/secret.md'));
  assert.throws(() => resolveInside(root, '..'));
  assert.throws(() => resolveInside(root, path.join(sibling, 'secret.md')));
});

test('startsWith 비교는 notes-evil을 통과시키는 함정이 있다', () => {
  // 문자열 비교만 쓰면 이 값이 true라서 뚫린다
  assert.equal(path.join(sibling, 'secret.md').startsWith(root), true);
  assert.throws(() => resolveInside(root, '../notes-evil/secret.md'));
});

test('루트 밖을 가리키는 심볼릭 링크를 따라가면 막는다', (t) => {
  try {
    symlinkSync(sibling, path.join(root, 'link'), 'junction');
  } catch {
    t.skip('이 환경에서는 링크를 만들 수 없음');
    return;
  }
  assert.throws(() => resolveInside(root, 'link/secret.md'));
});
