package dev.workhard.dialect;

import java.util.Map;

/** DB 방언별 SQL 모음과 검사 도구. */
public final class SqlCatalog {
    private SqlCatalog() {}

    public static final Map<String, String> ORACLE = Map.of(
            "findOtherRooms", "SELECT * FROM room WHERE room_id NOT LIKE #{sid} || '-' || #{rid}",
            "countByNode", "SELECT node_id, COUNT(*) FROM room GROUP BY node_id");

    public static final Map<String, String> MARIADB = Map.of(
            // || 대신 CONCAT 사용
            "findOtherRooms", "SELECT * FROM room WHERE room_id NOT LIKE CONCAT(#{sid}, '-', #{rid})",
            "countByNode", "SELECT node_id, COUNT(*) FROM room GROUP BY node_id");

    // 문자열 리터럴 밖에 || 가 있으면 true (간단 검사)
    public static boolean usesPipeConcat(String sql) {
        String withoutLiterals = sql.replaceAll("'[^']*'", "''");
        return withoutLiterals.contains("||");
    }
}
