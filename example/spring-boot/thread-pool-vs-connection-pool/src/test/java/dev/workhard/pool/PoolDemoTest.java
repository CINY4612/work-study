package dev.workhard.pool;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLTransientConnectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

class PoolDemoTest {

    @Test
    void slowCallInsideConnectionExhaustsPool() throws Exception {
        try (HikariDataSource ds = PoolDemo.newPool(2, 300)) {
            // 스레드 2개가 커넥션을 쥔 채 1.5초 동안 대기
            Thread t1 = new Thread(() -> run(() -> PoolDemo.holdWhileSlow(ds, 1500)));
            Thread t2 = new Thread(() -> run(() -> PoolDemo.holdWhileSlow(ds, 1500)));
            t1.start();
            t2.start();

            // 풀의 커넥션 2개가 모두 사용 중이 될 때까지 기다린다
            while (ds.getHikariPoolMXBean().getActiveConnections() < 2) {
                Thread.sleep(10);
            }

            // 세 번째 요청은 300ms 안에 커넥션을 못 얻어 실패한다
            assertThrows(SQLTransientConnectionException.class, ds::getConnection);

            t1.join();
            t2.join();
        }
    }

    @Test
    void slowCallOutsideConnectionKeepsPoolFree() throws Exception {
        try (HikariDataSource ds = PoolDemo.newPool(2, 300)) {
            // 요청 스레드는 8개, 커넥션은 2개지만 커넥션은 아주 짧게만 쓴다
            ExecutorService requests = Executors.newFixedThreadPool(8);
            List<Future<?>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                results.add(requests.submit(() -> {
                    PoolDemo.slowThenQuery(ds, 200);
                    return null;
                }));
            }
            for (Future<?> f : results) {
                assertDoesNotThrow(() -> f.get());
            }
            requests.shutdown();
        }
    }

    interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void run(ThrowingRunnable r) {
        try {
            r.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
