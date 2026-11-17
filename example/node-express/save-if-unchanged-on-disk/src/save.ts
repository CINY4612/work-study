import { readFile, writeFile } from 'node:fs/promises';
import { createHash } from 'node:crypto';

export const hashOf = (s: string) => createHash('sha256').update(s).digest('hex');

export type SaveResult =
  | { ok: true }
  | { ok: false; reason: 'changed-on-disk' | 'missing' };

// loadedHash: 파일을 처음 읽었을 때의 내용 해시
export async function saveIfUnchanged(
  path: string,
  loadedHash: string,
  content: string,
): Promise<SaveResult> {
  const current = await readFile(path, 'utf8').catch(() => null);
  if (current === null) return { ok: false, reason: 'missing' };
  // 저장 직전에 다시 읽어 비교: 다르면 외부 변경이 있었다는 뜻
  if (hashOf(current) !== loadedHash) return { ok: false, reason: 'changed-on-disk' };
  await writeFile(path, content);
  return { ok: true };
}
