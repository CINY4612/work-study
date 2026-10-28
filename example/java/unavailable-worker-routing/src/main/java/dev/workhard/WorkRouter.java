package dev.workhard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 휴가 중인 작업자를 건너뛰고 조정자에게 되돌리는 라우터. */
public class WorkRouter {

    public enum Status { ACTIVE, LEAVING, AWAY }

    public static final int BOUNCE_LIMIT = 3;

    private final String coordinator;
    private final Map<String, Status> status = new HashMap<>();
    private final Map<String, Integer> pending = new HashMap<>();
    private final Map<String, Integer> bounces = new HashMap<>();
    private final List<String> alerts = new ArrayList<>();

    public WorkRouter(String coordinator) {
        this.coordinator = coordinator;
        status.put(coordinator, Status.ACTIVE);
    }

    public void register(String worker) {
        status.put(worker, Status.ACTIVE);
    }

    public Status status(String worker) {
        return status.getOrDefault(worker, Status.ACTIVE);
    }

    public List<String> alerts() {
        return alerts;
    }

    /** 휴가 보내기: 조정자는 불가, 일이 남았으면 LEAVING(마무리 중)으로 둔다. */
    public void sendOnLeave(String worker) {
        if (worker.equals(coordinator)) {
            throw new IllegalArgumentException("조정자는 휴가를 갈 수 없음");
        }
        boolean hasWork = pending.getOrDefault(worker, 0) > 0;
        status.put(worker, hasWork ? Status.LEAVING : Status.AWAY);
    }

    /** 배정: 가용하면 본인, 아니면 조정자에게 되돌린다. */
    public String assign(String worker) {
        if (status(worker) == Status.ACTIVE) {
            pending.merge(worker, 1, Integer::sum);
            return worker;
        }
        int n = bounces.merge(worker, 1, Integer::sum);
        if (n == BOUNCE_LIMIT + 1) { // 한도를 처음 넘는 순간에만 알림
            alerts.add(worker + " 앞 요청이 반복해서 되돌려짐");
        }
        return coordinator;
    }

    /** 일 완료: 마지막 일이 끝나면 LEAVING -> AWAY. */
    public void finish(String worker) {
        int left = pending.merge(worker, -1, Integer::sum);
        if (left == 0 && status(worker) == Status.LEAVING) {
            status.put(worker, Status.AWAY);
        }
    }

    /** 복귀: 되돌림 횟수도 초기화한다. */
    public void comeBack(String worker) {
        status.put(worker, Status.ACTIVE);
        bounces.remove(worker);
    }
}
