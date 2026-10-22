import type { RequestListener } from 'node:http';

// 앱 서버: SockJS 엔드포인트는 /ws 하나뿐이다 (prefix 모름)
export const upstream: RequestListener = (req, res) => {
  const url = new URL(req.url ?? '/', 'http://localhost');
  res.setHeader('content-type', 'application/json');

  // SockJS 클라이언트가 연결 전에 보내는 사전 요청
  if (req.method === 'GET' && url.pathname === '/ws/info') {
    res.end(JSON.stringify({ websocket: true, cookie_needed: false, origins: ['*:*'] }));
    return;
  }
  // 등록되지 않은 경로(예: /1/ws/info)는 404
  res.statusCode = 404;
  res.end(JSON.stringify({ error: 'not found', path: url.pathname }));
};
