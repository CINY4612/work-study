import { rename as fsRename, rm, writeFile } from 'node:fs/promises';
import path from 'node:path';

// rename을 인자로 받아 테스트에서 실패 상황을 만들 수 있게 한다
export async function writeAtomic(
  file: string,
  data: string,
  rename: typeof fsRename = fsRename,
): Promise<void> {
  // 임시 파일은 같은 폴더에 둔다 (다른 파일 시스템이면 rename이 원자적이지 않다)
  const tmp = path.join(path.dirname(file), `.${path.basename(file)}.${process.pid}.tmp`);
  try {
    await writeFile(tmp, data); // 1. 완성본을 임시 파일에 쓴다
    await rename(tmp, file); // 2. 한 번에 교체한다
  } catch (e) {
    await rm(tmp, { force: true }); // 실패하면 임시 파일을 남기지 않는다
    throw e;
  }
}
