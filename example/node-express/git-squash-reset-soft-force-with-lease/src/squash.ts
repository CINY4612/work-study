import { execFileSync } from 'node:child_process';

// 테스트가 전역 git 설정과 무관하게 돌도록 사용자/서명 설정을 옵션으로 넣는다
export function git(cwd: string, ...args: string[]): string {
  return execFileSync(
    'git',
    ['-c', 'user.name=tester', '-c', 'user.email=tester@example.com', '-c', 'commit.gpgsign=false', ...args],
    { cwd, encoding: 'utf8' },
  ).trim();
}

// base 이후의 커밋을 모두 풀어서(staged로 남김) 커밋 하나로 다시 만든다
export function squashSince(dir: string, base: string, message: string): void {
  git(dir, 'reset', '--soft', base);
  git(dir, 'commit', '-m', message);
}
