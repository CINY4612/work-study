<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [sql-dialect, mybatis, mariadb, oracle, parity-test]
date: 2026-11-17
example: example/java/sql-dialect-pipe-concat-parity
status: auto
-->

# DB 방언 SQL 파일 둘의 목록 일치를 테스트로 지키기

> [!NOTE]
> `||`는 Oracle에서는 문자열 연결이지만 MariaDB(기본 모드)에서는 OR이라, 에러 없이 틀린 결과가 나온다. 방언별 SQL은 CONCAT으로 맞춘다.

> [!WARNING]
> **헷갈린 점** · DB별 SQL 파일을 따로 두면 한쪽만 고치기 쉬우니, 문장 id 목록 일치와 금지 문법을 테스트로 검사한다.

## 📖 자세히

### 왜 이렇게 동작할까
- Oracle의 `a || '-' || b`는 문자열 연결이다. MariaDB/MySQL 기본 SQL 모드에서 `||`는 논리 OR이라서, 쿼리는 문법 오류 없이 실행되고 **조건이 엉뚱하게 참/거짓**이 된다. 에러가 안 나서 더 위험하다.
- 해결: 두 DB에서 모두 되는 `CONCAT(a, '-', b)`를 쓴다.
- DB를 설정값으로 고르는 구조(방언별 매퍼 파일 하나만 로딩)라면 두 파일의 **문장 id 목록이 같아야** 같은 자바 코드로 호출할 수 있다. 한쪽만 고치는 실수를 테스트가 잡게 한다.

```java
// 잘못된 예 (MariaDB에서는 OR로 해석됨)
"... WHERE id NOT LIKE #{sid} || '-' || #{rid}"
// 올바른 예 (양쪽 DB 공통)
"... WHERE id NOT LIKE CONCAT(#{sid}, '-', #{rid})"
```

```java
// 테스트: 문장 id 불일치와 금지 문법을 함께 검사
assertEquals(oracle.keySet(), mariadb.keySet());
mariadb.values().forEach(sql -> assertFalse(SqlCatalog.usesPipeConcat(sql)));
```

## 🔎 더 공부할 것

- SQL_MODE PIPES_AS_CONCAT - MariaDB에서 || 동작을 바꾸는 설정
- MERGE vs INSERT ... ON DUPLICATE KEY - 방언별 upsert 차이
- MyBatis databaseId - 한 XML에서 DB별 분기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#sql-dialect` `#mybatis` `#mariadb` `#oracle` `#parity-test` | 2026-11-17 | [예제 보기](../../example/java/sql-dialect-pipe-concat-parity) |
