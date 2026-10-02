package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class BankTest {

    @Test
    void oppositeTransfersDoNotDeadlockAndKeepTotal() {
        Bank bank = new Bank();
        Account a = new Account(1, 1_000);
        Account b = new Account(2, 1_000);

        // 데드락이 나면 join이 끝나지 않으므로 타임아웃으로 감지한다
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            Thread t1 = new Thread(() -> repeat(() -> bank.transfer(a, b, 1)));
            Thread t2 = new Thread(() -> repeat(() -> bank.transfer(b, a, 1)));
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        });

        // 이체는 합계를 바꾸지 않는다
        assertEquals(2_000, a.balance() + b.balance());
    }

    private static void repeat(Runnable r) {
        for (int i = 0; i < 20_000; i++) {
            r.run();
        }
    }
}
