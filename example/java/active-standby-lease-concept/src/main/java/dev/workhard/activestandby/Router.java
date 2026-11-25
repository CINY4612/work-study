package dev.workhard.activestandby;

import java.util.Map;

public class Router {
    private final LeaseStore store;
    private final Map<String, Node> nodes;

    public Router(LeaseStore store, Map<String, Node> nodes) {
        this.store = store;
        this.nodes = nodes;
    }

    /** 노드 자신의 판단이 아니라 저장소의 현재 보유자에게만 보낸다. */
    public String send(String request) {
        String holder = store.holder();
        if (holder == null) {
            throw new IllegalStateException("no active node");
        }
        return nodes.get(holder).handle(request);
    }
}
