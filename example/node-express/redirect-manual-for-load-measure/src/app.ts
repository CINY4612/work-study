import express from 'express';

// 진입점: 요청마다 노드 1, 2를 번갈아 가리키는 302를 준다
export function createApp() {
  const app = express();
  let n = 0;
  app.get('/enter', (_req, res) => {
    n++;
    res.redirect(302, `/node/${(n % 2) + 1}/home`);
  });
  app.get('/node/:id/home', (req, res) => {
    res.send(`node ${req.params.id}`);
  });
  return app;
}
