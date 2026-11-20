import test from "node:test";
import assert from "node:assert/strict";
import { mkdtempSync, writeFileSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { git } from "../src/git.js";

const lines = (n2: string, n18: string) =>
  Array.from({ length: 20 }, (_, i) =>
    i === 1 ? n2 : i === 17 ? n18 : `line${i + 1}`,
  ).join("\n") + "\n";

test("한 파일의 두 hunk를 두 커밋으로 나눈다", () => {
  const dir = mkdtempSync(join(tmpdir(), "split-"));
  try {
    git(dir, ["init", "-q"]);
    git(dir, ["config", "user.name", "tester"]);
    git(dir, ["config", "user.email", "t@example.com"]);
    git(dir, ["config", "core.autocrlf", "false"]);

    writeFileSync(join(dir, "a.txt"), lines("line2", "line18"));
    git(dir, ["add", "a.txt"]);
    git(dir, ["commit", "-q", "-m", "base"]);

    // 서로 멀리 떨어진 두 곳(2번째 줄, 18번째 줄)을 수정 -> hunk 2개
    writeFileSync(join(dir, "a.txt"), lines("CHANGE-A", "CHANGE-B"));

    // 첫 hunk는 y(담기), 둘째는 n(건너뛰기)
    git(dir, ["add", "-p"], "y\nn\n");
    const staged = git(dir, ["diff", "--cached"]);
    assert.ok(staged.includes("CHANGE-A"));
    assert.ok(!staged.includes("CHANGE-B"));
    git(dir, ["commit", "-q", "-m", "A 작업"]);

    // 건너뛴 변경은 작업 트리에 남아 있다
    assert.ok(git(dir, ["diff"]).includes("CHANGE-B"));
    git(dir, ["commit", "-q", "-am", "B 작업"]);

    const log = git(dir, ["log", "--format=%s"]).trim().split("\n");
    assert.deepEqual(log, ["B 작업", "A 작업", "base"]);
    // 첫 커밋에는 A만 들어 있다
    const first = git(dir, ["show", "HEAD~1"]);
    assert.ok(first.includes("CHANGE-A") && !first.includes("CHANGE-B"));
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});
