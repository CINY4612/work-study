package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ShelfServiceTest {

    private final ShelfService service = new ShelfService(Map.of(
            "1", List.of("wh-a", "wh-b"),
            "2", List.of("wh-c")));

    @Test
    void nullOrBlankUsesDefaultShelf() {
        assertEquals(List.of("wh-a", "wh-b"), service.candidateWarehouses(null));
        assertEquals(List.of("wh-a", "wh-b"), service.candidateWarehouses("  "));
    }

    @Test
    void explicitShelfIsRespected() {
        assertEquals(List.of("wh-c"), service.candidateWarehouses("2"));
    }

    @Test
    void unknownShelfIsEmptyNotDefault() {
        // 오타 같은 미등록 값을 기본값으로 덮으면 문제가 숨겨진다
        assertEquals(List.of(), service.candidateWarehouses("999"));
    }

    @Test
    void bothEntryPointsBehaveTheSame() {
        OrderApi api = new OrderApi(service);
        assertEquals(api.pickFromQuery(null), api.pickFromBody(null));
        assertEquals("wh-a", api.pickFromQuery(null));
        assertNull(api.pickFromBody("999"));
    }
}
