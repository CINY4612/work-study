package dev.workhard.activestandby;

public class Node {
    private final String id;
    private final LeaseStore store;
    private boolean active;

    public Node(String id, LeaseStore store) {
        this.id = id;
        this.store = store;
    }

    /** 주기 작업 한 번: 임대를 잡거나 갱신하고 결과를 기억한다. */
    public void tick() {
        active = store.tryAcquire(id);
    }

    public boolean isActive() {
        return active;
    }

    public String handle(String request) {
        if (!active) {
            throw new IllegalStateException(id + " is standby"); // Standby는 처리하지 않는다
        }
        return id + ":" + request;
    }
}
