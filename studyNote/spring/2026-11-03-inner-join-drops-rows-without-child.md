<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [sql, inner-join, left-join, jdbc]
date: 2026-11-03
example: example/spring-boot/inner-join-drops-rows-without-child
status: auto
-->

# INNER JOIN이 설정 행 없는 계정을 지우는 문제

> [!NOTE]
> INNER JOIN은 짝이 되는 행이 없으면 부모 행도 결과에서 빼므로, 설정 테이블에 행이 없는 계정은 조회 0건이 된다.

> [!WARNING]
> **헷갈린 점** · 계정은 있는데 '없는 ID'로 나오면 에러가 아니라 조인 때문에 사라진 것이다. LEFT JOIN으로 짝 없음을 구분할 수 있다.

## 📖 자세히

### 왜 이렇게 동작할까
INNER JOIN은 양쪽 테이블에 조건이 맞는 행이 모두 있을 때만 결과에 넣는다. 그래서 `member`에는 행을 넣었는데 `member_setting`에 짝이 되는 행을 안 넣으면, 로그인 같은 조회 쿼리가 0건을 돌려준다. 코드 입장에서는 오류 없이 '해당 계정 없음'으로 보이기 때문에 원인 찾기가 어렵다.

LEFT JOIN은 왼쪽 행을 모두 남기고, 짝이 없으면 오른쪽 컬럼을 NULL로 채운다. 그래서 '짝이 없다'는 사실을 쿼리로 확인할 수 있다.

```java
// INNER JOIN: 설정 행이 없으면 계정 자체가 사라진다
SELECT m.id FROM member m JOIN member_setting s ON s.member_id = m.id WHERE m.login_id = ?

// LEFT JOIN + IS NULL: 설정이 빠진 계정을 찾아낸다
SELECT m.id FROM member m LEFT JOIN member_setting s ON s.member_id = m.id WHERE s.member_id IS NULL
```

### 실무 팁
- 테스트 데이터를 직접 INSERT할 때는 조회 쿼리가 조인하는 연관 테이블까지 같이 채운다.
- '있어야 하는 짝'이 빠진 데이터는 위 LEFT JOIN ... IS NULL 쿼리로 먼저 점검한다.

## 🔎 더 공부할 것

- LEFT JOIN과 집계 - COUNT(컬럼)과 COUNT(*)의 차이
- FK 제약조건 - 부모와 자식 행이 짝을 이루도록 DB에서 강제하는 방법
- INSERT ... SELECT - 테스트 데이터를 한 번에 만드는 방법

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#sql` `#inner-join` `#left-join` `#jdbc` | 2026-11-03 | [예제 보기](../../example/spring-boot/inner-join-drops-rows-without-child) |
