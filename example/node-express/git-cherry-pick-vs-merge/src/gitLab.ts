import { execFileSync } from 'node:child_process';
import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

// 작성자(author)를 환경변수로 바꿔 가며 git을 실행한다
export function git(dir: string, args: string[], author = 'alice'): string {
  return execFileSync('git', args, {
    cwd: dir,
    encoding: 'utf8',
    env: {
      ...process.env,
      GIT_AUTHOR_NAME: author,
      GIT_AUTHOR_EMAIL: `${author}@example.com`,
      GIT_COMMITTER_NAME: 'committer',
      GIT_COMMITTER_EMAIL: 'committer@example.com',
    },
  }).trim();
}

export function commitFile(dir: string, file: string, content: string, message: string, author: string): void {
  writeFileSync(join(dir, file), content);
  git(dir, ['add', file], author);
  git(dir, ['commit', '-m', message], author);
}

// base 커밋 하나가 있는 임시 저장소를 만든다
export function initRepo(): { dir: string; base: string } {
  const dir = mkdtempSync(join(tmpdir(), 'git-lab-'));
  git(dir, ['init', '-b', 'main']);
  commitFile(dir, 'base.txt', 'base', 'base', 'carol');
  return { dir, base: git(dir, ['rev-parse', 'HEAD']) };
}

// base 위에 alice, bob이 쓴 커밋 2개를 가진 feature 브랜치를 만들고 main으로 돌아온다
export function addFeatureBranch(dir: string): void {
  git(dir, ['checkout', '-b', 'feature']);
  commitFile(dir, 'a.txt', 'a', 'add a', 'alice');
  commitFile(dir, 'b.txt', 'b', 'add b', 'bob');
  git(dir, ['checkout', 'main']);
}

export function log(dir: string, range: string, format: string): string[] {
  const out = git(dir, ['log', '--reverse', `--format=${format}`, range]);
  return out === '' ? [] : out.split('\n');
}
