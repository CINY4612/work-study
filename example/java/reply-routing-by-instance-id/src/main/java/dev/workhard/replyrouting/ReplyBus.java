package dev.workhard.replyrouting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 인스턴스 ID를 주소로 쓰는 응답 버스. */
public class ReplyBus {
    private record Sub(String id, Consumer<String> handler) {}

    private final List<Sub> subs = new ArrayList<>();
    private final boolean rejectDuplicate;

    public ReplyBus(boolean rejectDuplicate) {
        this.rejectDuplicate = rejectDuplicate;
    }

    public void register(String id, Consumer<String> handler) {
        if (rejectDuplicate && subs.stream().anyMatch(s -> s.id().equals(id))) {
            throw new IllegalStateException("duplicate instance id: " + id);
        }
        subs.add(new Sub(id, handler));
    }

    /** ID가 같은 모든 구독자에게 전달하고, 받은 수를 돌려준다. */
    public int reply(String targetId, String msg) {
        int n = 0;
        for (Sub s : subs) {
            if (s.id().equals(targetId)) {
                s.handler().accept(msg);
                n++;
            }
        }
        return n;
    }
}
