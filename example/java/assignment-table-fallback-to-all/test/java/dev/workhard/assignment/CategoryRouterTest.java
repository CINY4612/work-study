package dev.workhard.assignment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CategoryRouterTest {
    private final Set<String> all = Set.of("A", "B");
    private final CategoryRouter router = new CategoryRouter(all);

    @Test
    void unassignedCategoryUsesAllHealthyNodes() {
        assertEquals(List.of("A", "B"), router.candidates("book", all));
    }

    @Test
    void assignedCategoryUsesOnlyAssignedNodes() {
        router.replaceAssignments(Map.of("book", Set.of("B")));
        assertEquals(List.of("B"), router.candidates("book", all));
        assertEquals(List.of("A", "B"), router.candidates("toy", all)); // 다른 카테고리는 그대로
    }

    @Test
    void assignedNodesDownMeansNoCandidateNotFallback() {
        router.replaceAssignments(Map.of("book", Set.of("B")));
        assertTrue(router.candidates("book", Set.of("A")).isEmpty());
    }

    @Test
    void singleNodeAssignmentIsFlaggedAsRisk() {
        router.replaceAssignments(Map.of("book", Set.of("B"), "toy", Set.of("A", "B")));
        assertTrue(router.isSinglePointOfFailure("book"));
        assertFalse(router.isSinglePointOfFailure("toy"));
        assertFalse(router.isSinglePointOfFailure("none"));
    }

    @Test
    void replacingTableTakesEffectWithoutRestart() {
        router.replaceAssignments(Map.of("book", Set.of("A")));
        assertEquals(List.of("A"), router.candidates("book", all));
        router.replaceAssignments(Map.of());
        assertEquals(List.of("A", "B"), router.candidates("book", all));
    }
}
