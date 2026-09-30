// Parse a JSONL queue; broken lines are skipped instead of failing the whole batch.
export function readJsonl(text) {
  return text
    .split('\n')
    .filter((line) => line.trim())
    .flatMap((line) => {
      try {
        return [JSON.parse(line)];
      } catch {
        return [];
      }
    });
}

// Pinned todos always go first; auto-collected ones need enough changes, newest first, up to the cap.
export function pickCandidates({ pinned, collected, done = new Set(), minChanges = 2, max = 6 }) {
  const pinnedIds = new Set(pinned.map((t) => t.id));
  const auto = collected
    .filter((t) => !done.has(t.id) && !pinnedIds.has(t.id) && t.changes >= minChanges)
    .sort((a, b) => b.ts.localeCompare(a.ts));
  const room = Math.max(0, max - pinned.length);
  return [...pinned, ...auto.slice(0, room)];
}

// The expensive step runs once per batch, and only when something qualified.
export async function runBatch(input, expensiveCall) {
  const picked = pickCandidates(input);
  if (picked.length === 0) return { called: false, picked };
  return { called: true, picked, result: await expensiveCall(picked) };
}
