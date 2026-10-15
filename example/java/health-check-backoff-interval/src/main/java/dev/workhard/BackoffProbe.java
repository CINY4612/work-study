package dev.workhard;

import java.time.Duration;
import java.time.Instant;

public final class BackoffProbe {

    private final Duration base;
    private final Duration max;
    private int failures;
    private Instant nextAt = Instant.MIN;

    public BackoffProbe(Duration base, Duration max) {
        this.base = base;
        this.max = max;
    }

    // 예약된 다음 확인 시각이 지났는지
    public boolean isDue(Instant now) {
        return !now.isBefore(nextAt);
    }

    public void record(boolean success, Instant now) {
        failures = success ? 0 : failures + 1; // 성공하면 기본 간격으로 복귀
        nextAt = now.plus(currentInterval());  // 다음 확인 시각 예약
    }

    public Duration currentInterval() {
        int shift = Math.min(failures, 20);         // 시프트 오버플로 방지
        long millis = base.toMillis() << shift;     // base * 2^failures
        return Duration.ofMillis(Math.min(millis, max.toMillis())); // 상한 적용
    }
}
