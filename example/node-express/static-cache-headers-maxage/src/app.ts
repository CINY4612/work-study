import express from 'express';
import type { Express } from 'express';

// 정적 파일 앱을 만든다. maxAge를 주지 않으면 express.static 기본값(max-age=0)이 적용된다.
export function createStaticApp(dir: string, maxAge?: string): Express {
  const app = express();
  app.use(express.static(dir, maxAge ? { maxAge } : {}));
  return app;
}
