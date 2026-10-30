// 브라우저 Storage의 핵심 동작만 흉내 낸 가짜 저장소 (Node에는 기본 제공되지 않음)
export class FakeStorage {
  private map = new Map<string, string>();
  getItem(key: string): string | null {
    return this.map.has(key) ? (this.map.get(key) as string) : null; // 없으면 null
  }
  setItem(key: string, value: unknown): void {
    this.map.set(key, String(value)); // 항상 문자열로 변환
  }
}

export class Browser {
  local = new FakeStorage();
  session = new FakeStorage();
  // 탭을 닫으면 session만 새로 만들어지고 local은 남는다
  closeTab(): void {
    this.session = new FakeStorage();
  }
}

export function saveJson(s: FakeStorage, key: string, value: unknown): void {
  s.setItem(key, JSON.stringify(value));
}

export function loadJson<T>(s: FakeStorage, key: string, fallback: T): T {
  const raw = s.getItem(key);
  if (raw === null) return fallback;
  try {
    return JSON.parse(raw) as T;
  } catch {
    return fallback; // 깨진 값은 기본값으로
  }
}
