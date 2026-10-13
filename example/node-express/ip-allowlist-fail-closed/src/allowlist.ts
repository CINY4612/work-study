import type { RequestHandler } from 'express';

// '::ffff:127.0.0.1' 같은 IPv4-mapped IPv6 주소를 '127.0.0.1'로 바꾼다.
export function normalizeIp(ip: string | undefined): string {
  if (!ip) return '';
  return ip.startsWith('::ffff:') ? ip.slice('::ffff:'.length) : ip;
}

export function ipAllowlist(allowed: string[] | undefined): RequestHandler {
  return (req, res, next) => {
    const ip = normalizeIp(req.ip);
    // 설정이 없거나 비어 있으면 모두 거부한다 (fail-closed).
    if (!allowed || !allowed.includes(ip)) {
      res.status(403).json({ resultCode: 403 });
      return;
    }
    next();
  };
}
