import { test } from 'node:test';
import assert from 'node:assert/strict';
import { toScriptJson, escapeHtml, renderBootstrap } from '../src/script-json.js';

const evil = { name: '</script><script>alert(1)</script>' };

test('JSON.stringify만 쓰면 </script>가 그대로 남는다', () => {
  assert.ok(JSON.stringify(evil).includes('</script>'));
});

test('toScriptJson은 < > 를 이스케이프해서 script를 닫지 못하게 한다', () => {
  const out = toScriptJson(evil);
  assert.ok(!out.includes('</script>'));
  assert.ok(!out.includes('<'));
});

test('이스케이프해도 JS가 읽으면 원래 값과 같다', () => {
  assert.deepEqual(JSON.parse(toScriptJson(evil)), evil);
  const html = renderBootstrap({ userId: '42' });
  assert.ok(html.startsWith('<script>var g_params = {'));
});

test('escapeHtml은 텍스트용이라 JS 객체에는 쓸 수 없다', () => {
  assert.equal(escapeHtml('{"a":1}'), '{&quot;a&quot;:1}');
});
