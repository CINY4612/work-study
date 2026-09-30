import { readJsonl, runBatch } from './pick.js';

const collected = readJsonl(
  [
    '{"id":"t1","ts":"2026-01-01T10:00","changes":1}',
    '{"id":"t2","ts":"2026-01-01T11:00","changes":3}',
    'not json',
    '{"id":"t3","ts":"2026-01-01T12:00","changes":5}',
  ].join('\n'),
);
const pinned = [{ id: 'p1', ts: '2026-01-01T09:00', memo: 'always keep' }];

const out = await runBatch({ pinned, collected, max: 2 }, async (items) => `summary of ${items.length}`);
console.log(out.picked.map((t) => t.id), out.result);
