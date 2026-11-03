package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class MemberQueryTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired MemberQuery query;

    @BeforeEach
    void setUp() {
        jdbc.execute("DROP TABLE IF EXISTS member_setting");
        jdbc.execute("DROP TABLE IF EXISTS member");
        jdbc.execute("CREATE TABLE member (id BIGINT PRIMARY KEY, login_id VARCHAR(20))");
        jdbc.execute("CREATE TABLE member_setting (member_id BIGINT PRIMARY KEY, theme VARCHAR(10))");
        jdbc.update("INSERT INTO member VALUES (1, 'user001')");
        jdbc.update("INSERT INTO member VALUES (2, 'user002')");
        // user002에는 설정 행을 일부러 넣지 않는다
        jdbc.update("INSERT INTO member_setting VALUES (1, 'dark')");
    }

    @Test
    void memberWithSettingIsFound() {
        assertEquals(List.of(1L), query.findForLogin("user001"));
    }

    @Test
    void memberWithoutSettingVanishesInInnerJoin() {
        // 계정은 있지만 조회 결과는 0건 -> '없는 ID'처럼 보인다
        assertTrue(query.findForLogin("user002").isEmpty());
    }

    @Test
    void leftJoinFindsMembersMissingSetting() {
        assertEquals(List.of("user002"), query.findMembersWithoutSetting());
    }
}
