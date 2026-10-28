// 지정한 소스 줄(0부터)의 체크박스만 토글한다. 그 줄이 체크박스가 아니면 원문 그대로 반환.
const TASK = /^(\s*(?:[-*+]|\d+[.)])\s+\[)([ xX])(\]\s)/;

export function toggleTaskLine(source: string, line: number): string {
  // 캡처 그룹으로 split 하면 줄 구분자(\n 또는 \r\n)가 배열에 남아 그대로 합칠 수 있다
  const parts = source.split(/(\r?\n)/);
  const text = parts[line * 2];
  if (text === undefined || !TASK.test(text)) return source;
  parts[line * 2] = text.replace(
    TASK,
    (_m, head: string, mark: string, tail: string) => head + (mark === ' ' ? 'x' : ' ') + tail,
  );
  return parts.join('');
}
