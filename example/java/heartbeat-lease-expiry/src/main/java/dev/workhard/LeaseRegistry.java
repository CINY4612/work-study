package dev.workhard;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** 만료 시각 기반 lease 저장소. TTL 기능이 없는 저장소에서 heartbeat 생존 판정을 흉내 낸다. */
public class LeaseRegistry {

    private final Map<String, Instant> expiresAt = new ConcurrentHashMap<>();
    private final Clock clock; // 주입받아야 테스트에서 시간을 조작할 수 있다
    private final Duration ttl;

    public LeaseRegistry(Clock clock, Duration ttl) {
        this.clock = clock;
        this.ttl = ttl;
    }

    /** heartbeat: 만료 시각을 현재 + ttl 로 연장한다. */
    public void heartbeat(String id) {
        expiresAt.put(id, clock.instant().plus(ttl));
    }

    /** 만료 시각이 아직 오지 않았으면 살아 있다. */
    public boolean isAlive(String id) {
        Instant e = expiresAt.get(id);
        return e != null && clock.instant().isBefore(e);
    }

    /** 비어 있거나 만료된 경우에만 선점한다. 판단+저장을 compute 한 번에 묶어 원자적이다. */
    public boolean tryAcquire(String id) {
        Instant now = clock.instant();
        AtomicBoolean won = new AtomicBoolean(false);
        expiresAt.compute(id, (k, old) -> {
            if (old == null || !now.isBefore(old)) {
                won.set(true);
                return now.plus(ttl);
            }
            return old; // 아직 유효하면 그대로 둔다
        });
        return won.get();
    }
}
