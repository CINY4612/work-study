import express from 'express';
import { ipAllowlist } from './allowlist.js';

export interface AppConfig {
  allowedIps?: string[];
}

export function createApp(config: AppConfig) {
  const app = express();
  app.use(express.json());

  // 내부 전용 라우트에만 허용 목록을 건다.
  app.post('/internal/books/lookup', ipAllowlist(config.allowedIps), (_req, res) => {
    res.json({ resultCode: 200, titles: ['clean-code'] });
  });

  return app;
}
