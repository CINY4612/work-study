import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readJsonl, pickCandidates, runBatch } from '../src/pick.js';

const collected = [
  { id: 'a', ts: '2026-01-01T10:00', changes: 1 },
  { id: 'b', ts: '2026-01-01T11:00', changes: 3 },
  { id: 'c', ts: '2026-01-01T12:00', changes: 5 },
];

test('readJsonl skips blank and broken lines', () => {
  assert.deepEqual(readJsonl('{"id":1}\n\nbroken\n{"id":2}\n'), [{ id: 1 }, { id: 2 }]);
});

test('pinned first, then newest auto items above threshold, capped', () => {
  const picked = pickCandidates({ pinned: [{ id: 'p' }], collected, max: 2 });
  assert.deepEqual(picked.map((t) => t.id), ['p', 'c']);
});

test('done items are not picked again', () => {
  const picked = pickCandidates({ pinned: [], collected, done: new Set(['c']) });
  assert.deepEqual(picked.map((t) => t.id), ['b']);
});

test('expensive call is skipped when nothing qualifies', async () => {
  let calls = 0;
  const out = await runBatch({ pinned: [], collected, minChanges: 10 }, async () => calls++);
  assert.equal(out.called, false);
  assert.equal(calls, 0);
});

test('expensive call runs once for the whole batch', async () => {
  let calls = 0;
  const out = await runBatch({ pinned: [], collected }, async (items) => (calls++, items.length));
  assert.equal(calls, 1);
  assert.equal(out.result, 2);
});
