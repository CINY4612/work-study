<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [sql, left-join, group-by, jdbc, load-balancing]
date: 2026-10-16
example: example/spring-boot/left-join-zero-count-least-loaded
status: auto
-->

# LEFT JOIN으로 개수가 0인 항목까지 집계하기

> [!NOTE]
> '가장 한가한 서버'를 SQL로 고를 땐 서버 테이블을 기준으로 LEFT JOIN하고 COUNT(자식 컬럼)을 써야 0개인 서버가 후보에 남는다.

> [!WARNING]
> **헷갈린 점** · 자식 테이블에서 출발하거나 INNER JOIN을 쓰면 0건인 서버가 결과에서 빠져, 이미 쓰는 서버에만 계속 몰린다.

## 📖 자세히

### 왜 이렇게 동작할까
- INNER JOIN은 양쪽에 짝이 있는 행만 남깁니다. 방이 0개인 서버는 짝이 없어서 결과에서 사라집니다. 그래서 '가장 적게 쓰는 서버'를 고르면 이미 방이 있는 서버들 중에서만 고르게 됩니다.
- LEFT JOIN은 왼쪽(서버) 행을 모두 남기고, 짝이 없으면 오른쪽 컬럼을 NULL로 채웁니다.
- `COUNT(*)`는 NULL로 채워진 행도 1건으로 세므로 0개인 서버가 1로 나옵니다. `COUNT(r.room_id)`처럼 오른쪽 컬럼을 세야 NULL이 제외되어 0이 됩니다.
- 오른쪽 테이블에 대한 조건은 `WHERE`가 아니라 `ON`에 둡니다. WHERE에 두면 NULL 행이 걸러져 사실상 INNER JOIN으로 돌아갑니다.

### 조인 키는 PK 전체로
서버 PK가 (server_id, daemon_id)라면 daemon_id는 서버마다 겹칠 수 있습니다. 자식 테이블에도 server_id 쪽 컬럼을 두고 두 컬럼으로 조인해야 다른 서버의 같은 이름 daemon 개수가 섞이지 않습니다.

```sql
-- 서버(ms_node)를 기준으로 LEFT JOIN
SELECT m.server_id, m.daemon_id,
       COUNT(r.room_id) AS conn_cnt          -- COUNT(*) 아님: 0건이면 0
FROM ms_node m
LEFT JOIN room_node r
       ON r.ms_server_id = m.server_id       -- PK 전체로 조인
      AND r.daemon_id    = m.daemon_id
WHERE m.status = 1                           -- 기준 테이블 조건만 WHERE
GROUP BY m.server_id, m.daemon_id
ORDER BY conn_cnt, m.server_id, m.daemon_id  -- 동률이면 id로 결정적 정렬
```

## 🔎 더 공부할 것

- COUNT(*) vs COUNT(column) - NULL 처리 차이
- INNER/LEFT JOIN에서 ON과 WHERE 조건의 차이
- 복합 PK 조인 - 부분 키로 조인할 때 생기는 중복 집계
- 동률일 때 정렬 기준 - 결과를 결정적으로 만드는 이유

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#sql` `#left-join` `#group-by` `#jdbc` `#load-balancing` | 2026-10-16 | [예제 보기](../../example/spring-boot/left-join-zero-count-least-loaded) |
