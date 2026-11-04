# health-route

`Node 22 / TypeScript (tsx)` · 공통 프로젝트 `example/node-express` 의 의존성을 사용합니다.

## ▶ 실행

```bash
cd example/node-express
npm install
npx tsx health-route/src/server.ts
```

## ✅ 테스트

```bash
cd example/node-express
npm install
npm run test:one -- "health-route/test/*.test.ts"
```

## 📂 구조

```
src/app.ts
src/server.ts
test/app.test.ts
```

## 📝 관련 노트

- [Express 라우트를 앱 팩토리로 분리해 테스트](../../../studyNote/node-express/2026-09-30-app-factory-test.md)
