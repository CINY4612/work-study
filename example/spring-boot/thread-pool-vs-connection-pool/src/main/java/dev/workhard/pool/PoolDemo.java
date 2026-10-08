package dev.workhard.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.util.UUID;
import javax.sql.DataSource;

public class PoolDemo {

    // 커넥션이 size개뿐인 작은 풀 (H2 인메모리). 대기 한도는 timeoutMs
    public static HikariDataSource newPool(int size, long timeoutMs) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:" + UUID.randomUUID());
        config.setMaximumPoolSize(size);
        config.setConnectionTimeout(timeoutMs);
        return new HikariDataSource(config);
    }

    // 외부 서비스 호출처럼 오래 걸리는 작업을 흉내 낸다
    static void slowCall(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    // 나쁜 예: @Transactional 안에서 느린 호출 -> 대기하는 동안 커넥션을 계속 점유
    public static void holdWhileSlow(DataSource ds, long ms) throws Exception {
        try (Connection c = ds.getConnection()) {
            slowCall(ms);
            c.createStatement().execute("select 1");
        }
    }

    // 좋은 예: 느린 호출을 먼저 끝내고 DB 작업 순간에만 커넥션을 빌린다
    public static void slowThenQuery(DataSource ds, long ms) throws Exception {
        slowCall(ms);
        try (Connection c = ds.getConnection()) {
            c.createStatement().execute("select 1");
        }
    }
}
