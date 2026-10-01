package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// 테스트 메서드에 @Transactional을 붙이지 않는다: 붙이면 전체가 한 트랜잭션이 되어 차이를 볼 수 없다
@SpringBootTest
class AssignmentServiceTest {

    @Autowired
    AssignmentService service;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.execute("create table if not exists assignment (id int primary key, node_id int)");
        jdbc.update("delete from assignment");
        jdbc.update("insert into assignment values (1, 7)");
    }

    private int count() {
        return jdbc.queryForObject("select count(*) from assignment", Integer.class);
    }

    @Test
    void runtimeExceptionRollsBackDelete() {
        assertThrows(IllegalStateException.class, () -> service.cleanupAndFail(7));
        assertEquals(1, count()); // 롤백되어 행이 남아 있다
    }

    @Test
    void noRollbackForKeepsDelete() {
        assertThrows(IllegalStateException.class, () -> service.cleanupAndFailKeep(7));
        assertEquals(0, count()); // 예외가 나도 delete가 커밋되었다
    }
}
