package dev.workhard;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MemberQuery {
    private final JdbcTemplate jdbc;

    public MemberQuery(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // INNER JOIN: member_setting에 짝이 없으면 member가 있어도 결과에서 빠진다
    public List<Long> findForLogin(String loginId) {
        return jdbc.queryForList(
            "SELECT m.id FROM member m JOIN member_setting s ON s.member_id = m.id WHERE m.login_id = ?",
            Long.class, loginId);
    }

    // LEFT JOIN: 왼쪽(member)은 모두 남고, 짝이 없으면 s.member_id가 NULL
    public List<String> findMembersWithoutSetting() {
        return jdbc.queryForList(
            "SELECT m.login_id FROM member m LEFT JOIN member_setting s ON s.member_id = m.id WHERE s.member_id IS NULL ORDER BY m.login_id",
            String.class);
    }
}
