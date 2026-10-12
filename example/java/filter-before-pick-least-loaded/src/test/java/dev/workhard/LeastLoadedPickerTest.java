package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.workhard.LeastLoadedPicker.Node;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LeastLoadedPickerTest {

    // n1 이 전체 최소 부하지만 후보(n3, n4)에는 없다
    private final List<Node> alive = List.of(
            new Node("n1", 1), new Node("n2", 2), new Node("n3", 5), new Node("n4", 9));
    private final Set<String> candidates = Set.of("n3", "n4");

    @Test
    void filterFirstPicksBestInsideCandidates() {
        assertEquals("n3", LeastLoadedPicker.pickFilteredFirst(alive, candidates).orElseThrow().id());
    }

    @Test
    void pickThenCheckLosesTheFallback() {
        // 전체 1등(n1)이 범위 밖이라 범위 안에 노드가 있어도 결과가 비어 버린다
        assertTrue(LeastLoadedPicker.pickThenCheck(alive, candidates).isEmpty());
    }

    @Test
    void nullMeansNoRestrictionAndEmptyMeansNoTarget() {
        assertEquals("n1", LeastLoadedPicker.pickFilteredFirst(alive, null).orElseThrow().id());
        assertTrue(LeastLoadedPicker.pickFilteredFirst(alive, Set.of()).isEmpty());
    }
}
