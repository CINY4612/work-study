package dev.workhard.activestandby;

import java.util.function.LongSupplier;

/** 공유 저장소에 둔 임대(lease). 시간은 주입받아 테스트에서 흘려보낸다. */
public class LeaseStore {
    private final LongSupplier clock;
    private final long ttl;
    private String holder;
    private long expiresAt;
    private long term;

    public LeaseStore(LongSupplier clock, long ttl) {
        this.clock = clock;
        this.ttl = ttl;
    }

    public synchronized boolean tryAcquire(String nodeId) {
        long now = clock.getAsLong();
        boolean free = holder == null || now >= expiresAt; // 비었거나 만료
        if (free || holder.equals(nodeId)) {               // 새로 잡거나 갱신
            if (!nodeId.equals(holder)) {
                term++;                                    // 보유자가 바뀔 때만 증가
            }
            holder = nodeId;
            expiresAt = now + ttl;
            return true;
        }
        return false;
    }

    /** 유효한 보유자. 만료됐으면 null. */
    public synchronized String holder() {
        return holder != null && clock.getAsLong() < expiresAt ? holder : null;
    }

    public synchronized long term() {
        return term;
    }
}
