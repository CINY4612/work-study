import { createHmac, randomUUID, timingSafeEqual } from 'node:crypto';

export interface Claims {
  sub: string;
  exp: number; // 만료 시각(초)
  ver?: number; // 토큰 버전 (강제 로그아웃용)
}

const b64 = (s: string) => Buffer.from(s).toString('base64url');

function mac(data: string, secret: string): string {
  return createHmac('sha256', secret).update(data).digest('base64url');
}

export function signJwt(payload: Claims, secret: string): string {
  const head = b64(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = b64(JSON.stringify(payload));
  return `${head}.${body}.${mac(`${head}.${body}`, secret)}`;
}

// 서명과 만료만 본다. 저장소 조회가 전혀 없다는 점이 JWT의 특징
export function verifyJwt(token: string, secret: string, nowSec: number): Claims | null {
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  const expected = Buffer.from(mac(`${parts[0]}.${parts[1]}`, secret));
  const actual = Buffer.from(parts[2]);
  if (expected.length !== actual.length || !timingSafeEqual(expected, actual)) return null;
  const claims = JSON.parse(Buffer.from(parts[1], 'base64url').toString()) as Claims;
  return claims.exp > nowSec ? claims : null;
}

// 서버 세션 방식: 상태가 서버에 있으니 지우면 끝
export class SessionAuth {
  private sessions = new Map<string, string>(); // sid -> userId

  login(userId: string): string {
    const sid = randomUUID();
    this.sessions.set(sid, userId);
    return sid;
  }

  isValid(sid: string): boolean {
    return this.sessions.has(sid);
  }

  forceLogout(userId: string): void {
    for (const [sid, uid] of this.sessions) {
      if (uid === userId) this.sessions.delete(sid);
    }
  }
}

// JWT + 사용자별 버전: 강제 로그아웃을 위해 서버에 작은 상태를 둔다
export class VersionedJwtAuth {
  private versions = new Map<string, number>();

  constructor(private secret: string) {}

  issue(userId: string, nowSec: number, ttlSec = 60): string {
    const ver = this.versions.get(userId) ?? 0;
    return signJwt({ sub: userId, exp: nowSec + ttlSec, ver }, this.secret);
  }

  check(token: string, nowSec: number): boolean {
    const c = verifyJwt(token, this.secret, nowSec);
    return !!c && c.ver === (this.versions.get(c.sub) ?? 0);
  }

  forceLogout(userId: string): void {
    this.versions.set(userId, (this.versions.get(userId) ?? 0) + 1);
  }
}
