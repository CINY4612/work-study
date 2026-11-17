// script 태그 안에 넣어도 안전하도록 JSON의 위험 문자를 유니코드 이스케이프로 바꾼다
export function toScriptJson(value: unknown): string {
  return JSON.stringify(value)
    .replace(/</g, '\\u003c') // </script> 방지
    .replace(/>/g, '\\u003e')
    .replace(/&/g, '\\u0026')
    .replace(/\u2028/g, '\\u2028')
    .replace(/\u2029/g, '\\u2029');
}

// <%= %> 가 하는 일: HTML 텍스트용 이스케이프
export function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// 서버 데이터를 전역 변수로 심는 script 태그 생성
export function renderBootstrap(state: Record<string, unknown>): string {
  return `<script>var g_params = ${toScriptJson(state)};</script>`;
}
