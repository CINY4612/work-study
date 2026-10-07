import { randomBytes } from 'node:crypto';
import { sign, unsign } from './signedCookie.js';

export interface Session {
  sid: string;
  data: Record<string, unknown>;
  isNew: boolean;
}

export class SessionManager {
  // 세션 데이터는 서버에만 둔다 (실무에서는 DB/Redis)
  private store = new Map<string, Record<string, unknown>>();

  constructor(private secret: string) {}

  // 요청의 Cookie 헤더로 세션을 복원한다. 없거나 위조면 새 빈 세션
  open(cookieHeader?: string): Session {
    const match = cookieHeader?.match(/(?:^|;\s*)sid=([^;]+)/);
    const sid = match ? unsign(decodeURIComponent(match[1]), this.secret) : null;
    const saved = sid ? this.store.get(sid) : undefined;
    if (sid && saved) return { sid, data: { ...saved }, isNew: false };
    return { sid: randomBytes(16).toString('hex'), data: {}, isNew: true };
  }

  // 로그인 직후: 이전 sid를 버리고 새 sid 발급 (세션 고정 공격 방어)
  regenerate(session: Session): Session {
    this.store.delete(session.sid);
    return { sid: randomBytes(16).toString('hex'), data: {}, isNew: true };
  }

  // 응답 직전 호출. 반환값이 Set-Cookie 값이고, null이면 쿠키를 보내지 않는다
  commit(session: Session): string | null {
    // saveUninitialized: false 와 같은 동작 - 비어 있는 새 세션은 저장하지 않는다
    if (session.isNew && Object.keys(session.data).length === 0) return null;
    this.store.set(session.sid, session.data);
    if (!session.isNew) return null;
    return `sid=${encodeURIComponent(sign(session.sid, this.secret))}; HttpOnly; Path=/`;
  }
}
