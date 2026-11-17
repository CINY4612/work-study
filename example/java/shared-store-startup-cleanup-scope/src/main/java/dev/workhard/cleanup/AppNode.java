package dev.workhard.cleanup;

/** 기동할 때 이전 세션 키를 정리하는 노드. */
public class AppNode {
    private final String id;
    private final KeyValueStore store;
    private final boolean scopedCleanup;

    public AppNode(String id, KeyValueStore store, boolean scopedCleanup) {
        this.id = id;
        this.store = store;
        this.scopedCleanup = scopedCleanup;
    }

    public String sessionKey(String sessionId) {
        return "node:" + id + ":session:" + sessionId;
    }

    public void login(String sessionId) {
        store.put(sessionKey(sessionId), "alive");
    }

    public void start() {
        if (scopedCleanup) {
            // 자기 노드 접두어만 지운다
            store.deleteByPrefix("node:" + id + ":session:");
        } else {
            // 저장소를 혼자 쓴다고 가정한 전역 삭제 (공유 시 남의 키까지 지움)
            store.deleteByPrefix("node:");
        }
    }
}
