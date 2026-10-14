<!-- meta (read by scripts, hidden on GitHub)
category: node-express
tags: [multipart, multer, file-upload, express]
date: 2026-10-14
example: example/node-express/multipart-field-order
status: auto
-->

# multipart 업로드는 폼 필드를 파일보다 앞에 두기

> [!NOTE]
> multipart 본문은 앞에서부터 스트림으로 읽혀서, 파일 파트를 받는 시점에는 그보다 앞에 온 필드만 알 수 있다.

> [!WARNING]
> **헷갈린 점** · 저장 경로를 callId 같은 필드로 정한다면 그 필드를 파일 파트보다 먼저 append해야 한다.

## 📖 자세히

### 왜 순서가 중요할까

`multipart/form-data`는 boundary 문자열로 나뉜 파트들이 **보낸 순서대로** 이어진 본문입니다. multer 같은 스트리밍 파서는 본문 전체를 메모리에 모으지 않고 파트가 도착하는 대로 처리합니다. 그래서 파일 파트가 시작되어 저장 위치를 정하는 콜백(`destination`, `filename`)이 불리는 순간, `req.body`에는 **그 파트보다 앞에 도착한 텍스트 필드만** 들어 있습니다.

클라이언트가 파일을 먼저 append하면 콜백에서 `req.body.callId`가 `undefined`입니다. 에러 없이 엉뚱한 경로(예: `unknown`)에 저장되기 때문에 찾기 어렵습니다.

```ts
// 잘못된 예: 파일이 먼저 → 서버는 파일을 받을 때 callId를 아직 모른다
const bad = new FormData();
bad.append('file', new Blob(['hi']), 'raw.txt');
bad.append('callId', 'c1');

// 올바른 예: 경로 결정에 쓰는 필드를 먼저 append
const good = new FormData();
good.append('callId', 'c1');
good.append('fileName', 'a.txt');
good.append('file', new Blob(['hi']), 'raw.txt');
```

`FormData`는 append한 순서를 그대로 본문에 쓰므로, 순서를 정하는 책임은 보내는 쪽에 있습니다. 예제의 `resolveSavePath`는 파트를 앞에서부터 읽으며 첫 파일 파트에서 그때까지 본 필드로 경로를 정하는 방식을 흉내 냅니다.

## 🔎 더 공부할 것

- multer destination/filename 콜백 - 콜백 시점에 req.body에 무엇이 있는지 확인
- busboy - multer가 내부에서 쓰는 스트리밍 multipart 파서
- Content-Type boundary - 파트 경계가 어떻게 만들어지고 전달되는지
- https://developer.mozilla.org/docs/Web/API/FormData - FormData 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `node-express` | `#multipart` `#multer` `#file-upload` `#express` | 2026-10-14 | [예제 보기](../../example/node-express/multipart-field-order) |
