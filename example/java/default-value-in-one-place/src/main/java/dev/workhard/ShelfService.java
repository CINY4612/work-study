package dev.workhard;

import java.util.List;
import java.util.Map;

/** 선반 ID로 후보 창고를 찾는다. 기본값 규칙은 이 클래스에만 있다. */
public class ShelfService {

    public static final String DEFAULT_SHELF_ID = "1";

    private final Map<String, List<String>> warehousesByShelf;

    public ShelfService(Map<String, List<String>> warehousesByShelf) {
        this.warehousesByShelf = Map.copyOf(warehousesByShelf);
    }

    public List<String> candidateWarehouses(String shelfId) {
        // 포인트: null/빈 값 처리를 호출부가 아니라 여기서 한 번만 한다
        String effective = (shelfId == null || shelfId.isBlank())
                ? DEFAULT_SHELF_ID
                : shelfId;
        // 등록되지 않은 선반은 기본값으로 덮지 않고 빈 결과로 둔다
        return warehousesByShelf.getOrDefault(effective, List.of());
    }
}
