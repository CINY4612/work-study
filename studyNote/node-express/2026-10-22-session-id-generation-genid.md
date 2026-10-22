<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [express-session, sid, crypto, genid]
date: 2026-10-22
example: example/node-express/session-id-generation-genid
status: auto
-->

# express-session의 sid 생성 방식과 genid

> [!NOTE]
> express-session의 기본 sid는 crypto 난수 24바이트를 base64url로 바꾼 32자 문자열이며, 시간이나 노드 번호는 들어가지 않는다.

> [!WARNING]
> **헷갈린 점** · sid를 저장하는 컬럼이 32자보다 짧으면 저장이 실패한다. 길이를 맞춰야 하면 genid로 바이트 수를 줄인다.

## 📖 자세히

### 왜 유니크하다고 봐도 될까
express-session은 `genid` 옵션이 없으면 `uid-safe`로 sid를 만든다. 내부에서는 `crypto.randomBytes(24)`로 192비트 난수를 뽑고 base64url(URL에 안전한 base64)로 인코딩한다. base64url은 6비트가 문자 1개이므로 24바이트는 32자가 된다.

중복을 검사하는 로직은 없다. 난수 공간이 커서 겹칠 확률이 사실상 0이기 때문에 "유니크하다고 봐도 되는" 구조다. 생일 역설로 근사하면 세션 n개일 때 충돌 확률은 `n(n-1)/2 / 2^비트수`이다.

```ts
import { randomBytes } from 'node:crypto';

// 바이트 수 → 문자 수: ceil(bytes * 8 / 6)
export const generateSid = (bytes = 24) => randomBytes(bytes).toString('base64url');

// genid 옵션에 넣어 길이를 줄이는 예: 15바이트 → 정확히 20자
// session({ genid: () => generateSid(15), ... })
```

### 주의할 점
- 같은 sid가 생기면 스토어가 upsert(MERGE)를 쓰는 경우 에러 없이 기존 세션을 덮어쓴다. 확률이 낮을 뿐 감지하지는 못한다.
- sid를 짧게 줄이면 엔트로피(예측 불가능성)도 줄어든다. 15바이트(120비트)는 UUID v4(122비트)와 비슷한 수준이다.
- 쿠키에는 `s:<sid>.<서명>` 형태로 더 길게 들어가지만, 스토어 키로 쓰는 값은 서명을 뺀 sid이다.
- `base64url` 인코딩은 Node 15.7 이상에서 지원한다.

## 🔎 더 공부할 것

- uid-safe - express-session이 sid를 만들 때 쓰는 라이브러리
- 생일 역설 - 충돌 확률 근사식이 왜 n²에 비례하는지
- session fixation - 로그인 시 regenerate로 sid를 새로 발급하는 이유

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#express-session` `#sid` `#crypto` `#genid` | 2026-10-22 | [예제 보기](../../example/node-express/session-id-generation-genid) |
