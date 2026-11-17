// caret(^) 범위 개념을 흉내 낸 최소 구현 (메이저 > 0 인 경우만 다룬다)
function parse(v: string): [number, number, number] {
  const [a, b, c] = v.split('.').map(Number);
  return [a, b, c];
}

export function satisfiesCaret(version: string, range: string): boolean {
  const [maj, min, pat] = parse(version);
  const [rMaj, rMin, rPat] = parse(range.replace('^', ''));
  if (maj !== rMaj) return false; // 메이저가 다르면 audit fix가 건드리지 않는다
  return min > rMin || (min === rMin && pat >= rPat);
}

// audit fix가 하는 일: 범위 안에서 가장 높은 패치 버전을 고른다
export function pickPatched(range: string, available: string[]): string | undefined {
  return available
    .filter((v) => satisfiesCaret(v, range))
    .sort((x, y) => {
      const a = parse(x);
      const b = parse(y);
      return a[0] - b[0] || a[1] - b[1] || a[2] - b[2];
    })
    .pop();
}
