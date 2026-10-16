package dev.workhard;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoadRepository {

    public record Load(String serverId, String daemonId, int connCount) {}

    private final JdbcTemplate jdbc;

    public LoadRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 잘못된 예: INNER JOIN이라 방이 0개인 서버가 결과에서 사라진다
    public List<Load> loadsInnerJoin() {
        return jdbc.query("""
            SELECT m.server_id, m.daemon_id, COUNT(r.room_id) AS conn_cnt
            FROM ms_node m
            JOIN room_node r
              ON r.ms_server_id = m.server_id AND r.daemon_id = m.daemon_id
            WHERE m.status = 1
            GROUP BY m.server_id, m.daemon_id
            ORDER BY conn_cnt, m.server_id, m.daemon_id
            """, (rs, i) -> new Load(rs.getString(1), rs.getString(2), rs.getInt(3)));
    }

    // 올바른 예: 서버 기준 LEFT JOIN + COUNT(자식 컬럼) + PK 전체로 조인
    public List<Load> loads() {
        return jdbc.query("""
            SELECT m.server_id, m.daemon_id, COUNT(r.room_id) AS conn_cnt
            FROM ms_node m
            LEFT JOIN room_node r
              ON r.ms_server_id = m.server_id AND r.daemon_id = m.daemon_id
            WHERE m.status = 1
            GROUP BY m.server_id, m.daemon_id
            ORDER BY conn_cnt, m.server_id, m.daemon_id
            """, (rs, i) -> new Load(rs.getString(1), rs.getString(2), rs.getInt(3)));
    }

    public Load leastLoaded() {
        return loads().get(0);
    }
}
