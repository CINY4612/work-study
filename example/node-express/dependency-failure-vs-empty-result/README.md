# dependency-failure-vs-empty-result

`Node 22 / TypeScript (tsx)` · 공통 프로젝트 `example/node-express` 의 의존성을 사용합니다.

## ✅ 테스트

```bash
cd example/node-express
npm install
npm run test:one -- "dependency-failure-vs-empty-result/test/*.test.ts"
```

## 📂 구조

```
src/allocator.ts
src/app.ts
test/app.test.ts
```

## 📝 관련 노트

- [의존 서비스 장애(503)와 업무상 빈 결과(200)의 응답 구분](../../../studyNote/node-express/2026-10-07-dependency-failure-vs-empty-result.md)
