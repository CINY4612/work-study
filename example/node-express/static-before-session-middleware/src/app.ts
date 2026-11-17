import express from 'express';

// staticFirst 값에 따라 static / 세션 미들웨어 순서를 바꿔 볼 수 있는 앱
export function createApp(opts: { staticDir: string; staticFirst: boolean }) {
  const stats = { sessionLookups: 0 };
  const app = express();

  const staticMw = express.static(opts.staticDir);
  // 실제 세션 미들웨어는 저장소를 조회한다. 여기서는 조회 횟수만 센다.
  const sessionMw: express.RequestHandler = (_req, _res, next) => {
    stats.sessionLookups++;
    next();
  };

  if (opts.staticFirst) {
    app.use(staticMw); // 파일이 있으면 여기서 응답이 끝난다
    app.use(sessionMw);
  } else {
    app.use(sessionMw); // 모든 요청이 먼저 여기를 지난다
    app.use(staticMw);
  }

  app.get('/api/me', (_req, res) => {
    res.json({ ok: true });
  });

  return { app, stats };
}
