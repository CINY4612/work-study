import test from 'node:test';
import assert from 'node:assert/strict';
import { addFeatureBranch, commitFile, git, initRepo, log } from '../src/gitLab.js';

test('cherry-pick은 작성자를 유지하지만 해시는 새로 만든다', () => {
  const { dir, base } = initRepo();
  addFeatureBranch(dir);
  commitFile(dir, 'm.txt', 'm', 'main work', 'carol'); // main이 앞으로 나아간 상태

  git(dir, ['cherry-pick', `${base}..feature`], 'dave');

  assert.deepEqual(log(dir, `${base}..HEAD`, '%an').slice(1), ['alice', 'bob']); // 작성자 유지
  const original = log(dir, `${base}..feature`, '%H');
  const copied = log(dir, 'HEAD~2..HEAD', '%H');
  assert.notDeepEqual(copied, original); // 복사본이라 해시가 다름
});

test('merge --no-ff는 원래 해시를 유지하고 merge 커밋을 하나 더 만든다', () => {
  const { dir, base } = initRepo();
  addFeatureBranch(dir);
  const original = log(dir, `${base}..feature`, '%H');

  git(dir, ['merge', '--no-ff', 'feature', '-m', 'merge feature']);

  const all = git(dir, ['log', '--format=%H']).split('\n');
  for (const h of original) assert.ok(all.includes(h)); // 해시 그대로 이력에 존재
  const parents = git(dir, ['rev-list', '--parents', '-n', '1', 'HEAD']).split(' ');
  assert.equal(parents.length, 3); // 자기 자신 + 부모 2개
});

test('merge --squash 후 커밋하면 작성자가 커밋한 사람으로 바뀐다', () => {
  const { dir, base } = initRepo();
  addFeatureBranch(dir);

  git(dir, ['merge', '--squash', 'feature']);
  git(dir, ['commit', '-m', 'squashed'], 'squasher');

  assert.deepEqual(log(dir, `${base}..HEAD`, '%an'), ['squasher']); // alice, bob은 사라짐
});

test('staging에 남은 변경이 있으면 cherry-pick이 거부된다', () => {
  const { dir, base } = initRepo();
  addFeatureBranch(dir);
  commitFile(dir, 'stale.txt', 'v1', 'stale', 'carol');
  commitFile(dir, 'stale.txt', 'v2', 'stale again', 'carol');
  git(dir, ['reset', '--soft', 'HEAD~1']); // 마지막 변경을 staged 상태로 남김

  assert.throws(() => git(dir, ['cherry-pick', `${base}..feature`], 'dave'));

  // 범위 cherry-pick은 거부돼도 진행 상태(.git/sequencer)가 남아 다음 시도가 "already in progress"로 막힌다.
  // --quit은 작업 트리와 staging은 그대로 두고 진행 상태만 지운다(--abort는 HEAD를 되돌린다).
  git(dir, ['cherry-pick', '--quit']);
  git(dir, ['restore', '--staged', 'stale.txt']); // staging을 비우면 진행됨
  git(dir, ['cherry-pick', `${base}..feature`], 'dave');
  assert.deepEqual(log(dir, 'HEAD~2..HEAD', '%an'), ['alice', 'bob']);
});
