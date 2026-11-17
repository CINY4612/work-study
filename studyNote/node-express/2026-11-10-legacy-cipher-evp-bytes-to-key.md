<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [node, crypto, createCipher, migration]
date: 2026-11-10
example: example/node-express/legacy-cipher-evp-bytes-to-key
status: auto
-->

# createCipher 제거 후 기존 암호문 호환 유지

> [!NOTE]
> crypto.createCipher는 제거됐으므로 createCipheriv로 바꾸되, 기존 암호문을 풀려면 비밀번호에서 key/IV를 만드는 방식을 그대로 재현해야 한다.

> [!WARNING]
> **헷갈린 점** · 그냥 createCipheriv로 바꾸면 key/IV가 달라져 DB에 이미 저장된 암호문을 복호화할 수 없다.

## 📖 자세히

### 왜 단순 교체가 안 될까
`createCipher(algorithm, password)`는 비밀번호 문자열 하나만 받는다. 내부적으로 OpenSSL의 `EVP_BytesToKey`(MD5 해시, salt 없음, 반복 1회)로 key와 IV를 만들어 쓴다. 반면 `createCipheriv`는 key와 IV를 직접 넘겨야 한다. 그래서 새 코드에서 key/IV를 임의로 정하면 예전 암호문과 맞지 않는다.

1. `md5(비밀번호)`로 첫 16바이트를 만든다.
2. `md5(이전 해시 + 비밀번호)`를 이어 붙여 `key 길이 + IV 길이`만큼 채운다.
3. 앞부분은 key, 뒷부분은 IV로 자른다.
4. 그 key/IV로 `createCipheriv` / `createDecipheriv`를 쓴다.

```ts
import { createHash } from 'node:crypto';

export function evpBytesToKey(password: string, keyLen = 32, ivLen = 16) {
  const pw = Buffer.from(password, 'binary');
  let buf = Buffer.alloc(0);
  let prev = Buffer.alloc(0);
  while (buf.length < keyLen + ivLen) {
    // 이전 해시 + 비밀번호를 다시 MD5 -> 블록을 이어 붙인다
    prev = createHash('md5').update(Buffer.concat([prev, pw])).digest();
    buf = Buffer.concat([buf, prev]);
  }
  return { key: buf.subarray(0, keyLen), iv: buf.subarray(keyLen, keyLen + ivLen) };
}
```

### 주의
- MD5, salt 없음, IV가 비밀번호에서 결정되므로 **안전한 방식이 아니다**. 기존 데이터를 읽기 위한 호환용으로만 두고, 새 데이터는 랜덤 IV + 제대로 된 KDF(scrypt 등)로 저장하도록 옮겨 가는 계획을 세운다.
- 복호화에 try/catch가 있으면 실패가 빈 값으로 조용히 숨겨질 수 있으니, 교체 후 실제 기존 암호문으로 복호화 테스트를 해 본다.

## 🔎 더 공부할 것

- crypto.scrypt - salt를 쓰는 현대적인 키 유도 함수
- AES-GCM - 인증 태그로 변조까지 감지하는 모드
- 데이터 마이그레이션 - 기존 암호문을 새 방식으로 재암호화하는 절차

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#node` `#crypto` `#createCipher` `#migration` | 2026-11-10 | [예제 보기](../../example/node-express/legacy-cipher-evp-bytes-to-key) |
