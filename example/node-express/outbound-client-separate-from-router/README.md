# outbound-client-separate-from-router

`Node 22 / TypeScript (tsx)` · 공통 프로젝트 `example/node-express` 의 의존성을 사용합니다.

## ✅ 테스트

```bash
cd example/node-express
npm install
npm run test:one -- "outbound-client-separate-from-router/test/*.test.ts"
```

## 📂 구조

```
src/orderService.ts
src/paymentClient.ts
test/orderService.test.ts
```

## 📝 관련 노트

- [나가는 HTTP 호출을 라우터에서 클라이언트로 분리](../../../studyNote/node-express/2026-11-03-outbound-client-separate-from-router.md)
