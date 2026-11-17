import test from 'node:test';
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { evpBytesToKey, encrypt, decrypt } from '../src/legacyCipher.js';

test('key/IV 길이가 요청한 만큼 나온다', () => {
  const { key, iv } = evpBytesToKey('secret');
  assert.equal(key.length, 32);
  assert.equal(iv.length, 16);
});

test('첫 블록은 md5(비밀번호) 와 같다', () => {
  const { key } = evpBytesToKey('password');
  const md5 = createHash('md5').update('password').digest();
  assert.deepEqual(key.subarray(0, 16), md5);
});

test('같은 비밀번호면 암호화한 값을 다시 풀 수 있다', () => {
  const cipher = encrypt('010-0000-0000', 'secret');
  assert.equal(decrypt(cipher, 'secret'), '010-0000-0000');
});

test('같은 입력은 항상 같은 암호문이다 (IV가 고정이라 결정적)', () => {
  assert.equal(encrypt('hello', 'secret'), encrypt('hello', 'secret'));
});
