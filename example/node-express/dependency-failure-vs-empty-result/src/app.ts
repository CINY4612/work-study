import express from "express";
import { allocateWithRetry, StoreUnavailableError, type SlotStore } from "./allocator.js";

// 테스트에서 재시도 간격을 줄일 수 있도록 delayMs를 인자로 받는다.
export function createApp(store: SlotStore, delayMs = 300) {
  const app = express();

  app.post("/orders/:id/allocate", async (req, res) => {
    try {
      const slot = await allocateWithRetry(store, req.params.id, { attempts: 3, delayMs });
      // 자리 없음은 업무 결과이므로 200으로 알려준다.
      if (slot === null) return res.json({ result: "NOT_ALLOCATED" });
      return res.json({ result: "ALLOCATED", slot });
    } catch (e) {
      // 저장소 장애는 503 + 별도 코드로 내려서 '자리 없음'과 구분한다.
      if (e instanceof StoreUnavailableError) {
        return res.status(503).json({ result: "STORE_UNAVAILABLE" });
      }
      return res.status(500).json({ result: "ERROR" });
    }
  });

  return app;
}
