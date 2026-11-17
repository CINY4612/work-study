import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { git, squashSince } from '../src/squash.js';

function commitFile(dir: string, name: string, text: string) {
  writeFileSync(join(dir, name), text);
  git(dir, 'add', '.');
  git(dir, 'commit', '-m', `add ${name}`);
}

test('base 이후 커밋 3개가 1개로 합쳐지고 파일 내용(tree)은 같다', () => {
  const dir = mkdtempSync(join(tmpdir(), 'squash-'));
  git(dir, 'init');
  commitFile(dir, 'a.txt', 'a');
  const base = git(dir, 'rev-parse', 'HEAD');
  commitFile(dir, 'b.txt', 'b');
  commitFile(dir, 'c.txt', 'c');
  commitFile(dir, 'd.txt', 'd');

  const treeBefore = git(dir, 'rev-parse', 'HEAD^{tree}');
  squashSince(dir, base, 'Feat: 합친 커밋');

  assert.equal(git(dir, 'rev-list', '--count', 'HEAD'), '2'); // base + 합친 커밋
  assert.equal(git(dir, 'rev-parse', 'HEAD^'), base); // 부모는 base
  assert.equal(git(dir, 'rev-parse', 'HEAD^{tree}'), treeBefore); // 내용은 그대로
  assert.equal(git(dir, 'log', '-1', '--format=%s'), 'Feat: 합친 커밋');
});
