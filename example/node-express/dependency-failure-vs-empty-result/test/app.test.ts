import { test } from "node:test";
import assert from "node:assert/strict";
import { once } from "node:events";
import type { AddressInfo } from "node:net";
import { createApp } from "../src/app.js";
import { StoreUnavailableError, type SlotStore } from "../src/allocator.js";

// 호출 횟수를 세는 가짜 저장소. 호출 순서대로 결과(문자열, null, 에러)를 돌려준다.
function fakeStore(results: Array<string | null | Error>) {
  const store = {
    calls: 0,
    async tryAcquire(): Promise<string | null> {
      const r = results[Math.min(store.calls, results.length - 1)];
      store.calls++;
      if (r instanceof Error) throw r;
      return r;
    },
  };
  return store satisfies SlotStore & { calls: number };
}

async function post(store: SlotStore, path: string) {
  const server = createApp(store, 0).listen(0); // 재시도 간격 0ms로 빠르게
  await once(server, "listening");
  const { port } = server.address() as AddressInfo;
  try {
    const res = await fetch(`http://127.0.0.1:${port}${path}`, { method: "POST" });
    return { status: res.status, body: (await res.json()) as Record<string, unknown> };
  } finally {
    server.close();
  }
}

test("슬롯이 있으면 200 ALLOCATED", async () => {
  const store = fakeStore(["slot-1"]);
  const res = await post(store, "/orders/o1/allocate");
  assert.equal(res.status, 200);
  assert.deepEqual(res.body, { result: "ALLOCATED", slot: "slot-1" });
});

test("자리 없음은 200 NOT_ALLOCATED이고 재시도하지 않는다", async () => {
  const store = fakeStore([null]);
  const res = await post(store, "/orders/o1/allocate");
  assert.equal(res.status, 200);
  assert.equal(res.body.result, "NOT_ALLOCATED");
  assert.equal(store.calls, 1);
});

test("저장소 장애가 계속되면 3회 시도 후 503", async () => {
  const store = fakeStore([new StoreUnavailableError("down")]);
  const res = await post(store, "/orders/o1/allocate");
  assert.equal(res.status, 503);
  assert.equal(res.body.result, "STORE_UNAVAILABLE");
  assert.equal(store.calls, 3);
});

test("일시 장애 뒤 복구되면 재시도로 성공한다", async () => {
  const store = fakeStore([new StoreUnavailableError("failover"), "slot-2"]);
  const res = await post(store, "/orders/o1/allocate");
  assert.equal(res.status, 200);
  assert.equal(res.body.slot, "slot-2");
  assert.equal(store.calls, 2);
});
