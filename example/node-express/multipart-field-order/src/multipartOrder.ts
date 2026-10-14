export interface Part {
  name: string;
  filename?: string;
  value: string;
}

// multipart 본문을 boundary로 잘라 "도착한 순서대로" 파트 목록을 만든다.
export function parseParts(body: string, contentType: string): Part[] {
  const boundary = /boundary=(.+)$/.exec(contentType)?.[1];
  if (!boundary) throw new Error('boundary가 없습니다');
  return body
    .split(`--${boundary}`)
    .slice(1)
    .filter((raw) => !raw.startsWith('--')) // 마지막 닫는 boundary 제외
    .map((raw) => {
      const [head, ...rest] = raw.replace(/^\r\n/, '').split('\r\n\r\n');
      const value = rest.join('\r\n\r\n').replace(/\r\n$/, '');
      const name = /name="([^"]*)"/.exec(head)?.[1] ?? '';
      const filename = /filename="([^"]*)"/.exec(head)?.[1];
      return { name, filename, value };
    });
}

// 스트리밍 파서처럼 앞에서부터 읽다가, 첫 파일 파트를 만나는 순간 경로를 정한다.
// 그 시점에 "이미 본 필드"만 쓸 수 있다는 점이 핵심.
export function resolveSavePath(parts: Part[]): string {
  const seen: Record<string, string> = {};
  for (const part of parts) {
    if (part.filename === undefined) {
      seen[part.name] = part.value;
      continue;
    }
    const dir = seen.callId ?? 'unknown'; // 아직 못 봤으면 기본값으로 떨어진다
    const file = seen.fileName ?? part.filename;
    return `uploads/${dir}/${file}`;
  }
  throw new Error('파일 파트가 없습니다');
}
