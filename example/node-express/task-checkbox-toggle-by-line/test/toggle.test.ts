import { test } from 'node:test';
import assert from 'node:assert/strict';
import { toggleTaskLine } from '../src/toggle.js';

test('지정한 줄의 체크박스만 바뀐다', () => {
  const src = '- [ ] a\n- [ ] b\n- [x] c';
  assert.equal(toggleTaskLine(src, 1), '- [ ] a\n- [x] b\n- [x] c');
  assert.equal(toggleTaskLine(src, 2), '- [ ] a\n- [ ] b\n- [ ] c');
});

test('CRLF 줄바꿈을 보존한다', () => {
  const src = '- [ ] a\r\n- [ ] b\r\n';
  assert.equal(toggleTaskLine(src, 0), '- [x] a\r\n- [ ] b\r\n');
});

test('체크박스가 아닌 줄이나 범위 밖 줄은 원문을 그대로 돌려준다', () => {
  const src = '# 제목\n본문 [ ] 텍스트';
  assert.equal(toggleTaskLine(src, 0), src);
  assert.equal(toggleTaskLine(src, 1), src);
  assert.equal(toggleTaskLine(src, 9), src);
});
