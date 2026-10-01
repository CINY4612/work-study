package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// 테스트 메서드에 @Transactional을 붙이지 않는다: 붙이면 테스트가 끝날 때 롤백돼 afterCommit이 불리지 않는다
@SpringBootTest
class OrderServiceTest {

    @Autowired OrderService service;
    @Autowired OrderService.OrderNotifier notifier;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.execute("create table if not exists orders(id bigint auto_increment primary key, item varchar(50))");
        jdbc.update("delete from orders");
        notifier.sent().clear();
    }

    @Test
    void sendsNotificationAfterCommit() {
        service.place("book", false);

        assertEquals(1, jdbc.queryForObject("select count(*) from orders", Integer.class));
        assertEquals(java.util.List.of("book"), notifier.sent());
    }

    @Test
    void sendsNothingWhenRolledBack() {
        assertThrows(IllegalStateException.class, () -> service.place("book", true));

        assertEquals(0, jdbc.queryForObject("select count(*) from orders", Integer.class));
        assertTrue(notifier.sent().isEmpty());
    }
}
