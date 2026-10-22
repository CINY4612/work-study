import { randomBytes } from 'node:crypto';

// express-session 기본 생성기와 같은 방식: 난수 바이트를 base64url로 인코딩한다
export function generateSid(bytes = 24): string {
  return randomBytes(bytes).toString('base64url');
}

// base64url은 문자 1개가 6비트라서 바이트 수에서 문자 수가 정해진다
export function sidLength(bytes: number): number {
  return Math.ceil((bytes * 8) / 6);
}

// 생일 역설 근사: n개 중 어느 둘이 겹칠 확률 ≈ n(n-1)/2 / 2^bits
export function collisionProbability(sessions: number, bits: number): number {
  return (sessions * (sessions - 1)) / 2 / 2 ** bits;
}
