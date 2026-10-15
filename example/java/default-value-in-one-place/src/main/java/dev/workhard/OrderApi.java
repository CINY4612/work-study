package dev.workhard;

import java.util.List;

/** 선반 ID를 받는 두 진입점. 둘 다 null 분기 없이 서비스에 위임한다. */
public class OrderApi {

    private final ShelfService shelves;

    public OrderApi(ShelfService shelves) {
        this.shelves = shelves;
    }

    // 쿼리 파라미터로 들어오는 진입점
    public String pickFromQuery(String shelfId) {
        return first(shelves.candidateWarehouses(shelfId));
    }

    // 요청 본문으로 들어오는 진입점
    public String pickFromBody(String shelfId) {
        return first(shelves.candidateWarehouses(shelfId));
    }

    private String first(List<String> candidates) {
        return candidates.isEmpty() ? null : candidates.get(0);
    }
}
