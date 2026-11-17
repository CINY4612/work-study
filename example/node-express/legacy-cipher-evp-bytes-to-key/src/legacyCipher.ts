import { createCipheriv, createDecipheriv, createHash } from 'node:crypto';

// OpenSSL EVP_BytesToKey(MD5, salt 없음, 1회)를 재현한다.
// 옛 createCipher가 비밀번호에서 key/IV를 만들던 방식과 같다.
export function evpBytesToKey(password: string, keyLen = 32, ivLen = 16) {
  const pw = Buffer.from(password, 'binary');
  let buf = Buffer.alloc(0);
  let prev = Buffer.alloc(0);
  while (buf.length < keyLen + ivLen) {
    prev = createHash('md5').update(Buffer.concat([prev, pw])).digest();
    buf = Buffer.concat([buf, prev]);
  }
  return { key: buf.subarray(0, keyLen), iv: buf.subarray(keyLen, keyLen + ivLen) };
}

export function encrypt(text: string, password: string): string {
  const { key, iv } = evpBytesToKey(password);
  const c = createCipheriv('aes-256-cbc', key, iv);
  return Buffer.concat([c.update(text, 'utf8'), c.final()]).toString('hex');
}

export function decrypt(hex: string, password: string): string {
  const { key, iv } = evpBytesToKey(password);
  const d = createDecipheriv('aes-256-cbc', key, iv);
  return Buffer.concat([d.update(Buffer.from(hex, 'hex')), d.final()]).toString('utf8');
}
