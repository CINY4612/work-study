package dev.workhard.fencing;

import java.util.concurrent.atomic.AtomicLong;

// 리더 락을 새로 얻을 때마다 더 큰 토큰을 발급한다
public class TokenIssuer {
    private final AtomicLong counter = new AtomicLong();

    public long next() {
        return counter.incrementAndGet();
    }
}
