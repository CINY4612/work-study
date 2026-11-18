import { readFile, writeFile } from 'node:fs/promises';

// 내용이 달라졌을 때만 파일을 쓴다. 실제로 썼으면 true.
export async function writeIfChanged(path: string, next: Buffer | string): Promise<boolean> {
  const data = typeof next === 'string' ? Buffer.from(next) : next;
  try {
    const current = await readFile(path);
    if (current.equals(data)) return false; // 바이트가 같으면 mtime도 그대로 둔다
  } catch (e) {
    // 파일이 아직 없는 것만 정상 흐름, 나머지 오류는 그대로 던진다
    if ((e as NodeJS.ErrnoException).code !== 'ENOENT') throw e;
  }
  await writeFile(path, data);
  return true;
}
