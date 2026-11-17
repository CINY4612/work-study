package dev.workhard.replication;

/** 서버 2대 구성에서 자동 승격을 허용할지 판단하는 정족수 규칙. */
public final class Quorum {
    public enum Mode {
        /** 내 모니터에 보이는 서버들 중 과반이면 승격 */
        MAJORITY_OF_RUNNING,
        /** 전체 서버 수 기준 과반이어야 승격 */
        MAJORITY_OF_SERVERS
    }

    private Quorum() {}

    public static boolean mayPromote(Mode mode, int seenRunning, int total) {
        return switch (mode) {
            // 단절되면 양쪽 모두 '나 혼자 보임 = 보이는 것 중 과반' 이라 둘 다 true
            case MAJORITY_OF_RUNNING -> seenRunning >= 1;
            // 전체 2대 중 1대만 보이면 과반이 아니므로 false
            case MAJORITY_OF_SERVERS -> seenRunning > total / 2;
        };
    }
}
