package dev.workhard;

/**
 * 주기적 점검의 타임아웃 설정이 서로 맞는지 생성 시점에 검증한다.
 * 잘못된 조합이면 객체 자체를 만들 수 없게 해서 설정 실수를 바로 드러낸다.
 */
public record TimeoutBudget(long intervalMs, long probeTimeoutMs,
                            long callTimeoutMs, int attempts) {

    public TimeoutBudget {
        // 호출 쪽 타임아웃이 상대의 내부 프로브 타임아웃 이하이면,
        // 상대가 정상이어도 우리가 먼저 실패로 판단하게 된다
        if (callTimeoutMs <= probeTimeoutMs) {
            throw new IllegalArgumentException("callTimeout must be greater than probeTimeout");
        }
        // 전부 타임아웃 나는 최악의 경우에도 다음 주기 전에 끝나야 한다
        if (callTimeoutMs * attempts >= intervalMs) {
            throw new IllegalArgumentException("worst case must be shorter than interval");
        }
    }

    /** 모든 시도가 타임아웃까지 가는 최악의 소요 시간 */
    public long worstCaseMs() {
        return callTimeoutMs * attempts;
    }
}
