import { createHmac, timingSafeEqual } from 'node:crypto';

// 값 + HMAC 서명을 붙인다. 브라우저가 값을 바꾸면 서명이 어긋난다
export function sign(value: string, secret: string): string {
  const mac = createHmac('sha256', secret).update(value).digest('base64url');
  return `s:${value}.${mac}`;
}

// 서명이 맞으면 원래 값, 아니면 null
export function unsign(cookie: string, secret: string): string | null {
  if (!cookie.startsWith('s:')) return null;
  const idx = cookie.lastIndexOf('.');
  if (idx < 0) return null;
  const expected = Buffer.from(sign(cookie.slice(2, idx), secret));
  const actual = Buffer.from(cookie);
  // 길이가 다르면 timingSafeEqual이 예외를 던지므로 먼저 확인
  if (expected.length !== actual.length || !timingSafeEqual(expected, actual)) return null;
  return cookie.slice(2, idx);
}
