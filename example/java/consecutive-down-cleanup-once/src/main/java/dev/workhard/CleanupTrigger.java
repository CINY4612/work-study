package dev.workhard;

/** 연속 장애가 threshold번 이어지면 정리를 한 번만 요청한다. */
public class CleanupTrigger {
    private final int threshold;
    private int downCycles;
    private boolean cleaned;

    public CleanupTrigger(int threshold) {
        this.threshold = threshold;
    }

    /** 주기마다 호출한다. true가 반환되면 호출한 쪽이 정리를 실행한다. */
    public boolean onCycle(boolean down) {
        if (threshold <= 0) {
            return false; // 0 이하면 기능을 끈다
        }
        if (!down) {
            // 정상이면 연속 횟수와 정리 여부를 처음 상태로 되돌린다
            downCycles = 0;
            cleaned = false;
            return false;
        }
        downCycles++;
        if (downCycles >= threshold && !cleaned) {
            cleaned = true; // 장애가 이어지는 동안 한 번만
            return true;
        }
        return false;
    }

    /** 정리가 실패했으면 다음 주기에 다시 시도하도록 되돌린다. */
    public void cleanupFailed() {
        cleaned = false;
    }
}
