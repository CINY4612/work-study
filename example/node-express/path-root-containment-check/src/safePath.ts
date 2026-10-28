import { realpathSync } from 'node:fs';
import path from 'node:path';

// 존재하는 가장 가까운 경로를 realpath로 풀어 심볼릭 링크를 따라간 실제 위치를 구한다
function realOrParent(p: string): string {
  try {
    return realpathSync(p);
  } catch {
    // 아직 없는 파일(새로 저장)은 부모만 풀고 이름을 다시 붙인다
    return path.join(realOrParent(path.dirname(p)), path.basename(p));
  }
}

export function resolveInside(root: string, userPath: string): string {
  const realRoot = realpathSync(root);
  // 절대 경로가 오면 root는 무시되므로 반드시 아래 검사를 거친다
  const real = realOrParent(path.resolve(realRoot, userPath));
  const rel = path.relative(realRoot, real);
  // startsWith(root) 대신 relative 결과로 판단한다
  const escapes = rel === '..' || rel.startsWith('..' + path.sep) || path.isAbsolute(rel);
  if (escapes) throw new Error('허용된 루트 밖의 경로입니다');
  return real;
}
