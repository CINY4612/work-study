import { spawnSync } from "node:child_process";

// git을 실행하고 표준출력을 돌려준다. input은 대화형 질문(add -p)에 넣을 답이다.
export function git(cwd: string, args: string[], input?: string): string {
  const r = spawnSync("git", args, { cwd, input, encoding: "utf8" });
  if (r.status !== 0) {
    throw new Error(`git ${args.join(" ")} failed: ${r.stderr}`);
  }
  return r.stdout;
}
