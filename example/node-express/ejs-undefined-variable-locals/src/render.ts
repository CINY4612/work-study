import ejs from 'ejs';

// 템플릿: notSupport가 넘어오지 않을 수 있다
export const unsafeTemplate = '<% if (notSupport) { %>unsupported<% } else { %>normal<% } %>';
// locals.notSupport는 없으면 undefined
export const safeTemplate = '<% if (locals.notSupport) { %>unsupported<% } else { %>normal<% } %>';

export function render(template: string, data: Record<string, unknown> = {}): string {
  return ejs.render(template, data);
}
