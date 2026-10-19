package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TimeoutBudgetTest {

    @Test
    void worstCaseFitsInInterval() {
        TimeoutBudget budget = new TimeoutBudget(5000, 1000, 1500, 3);

        assertEquals(4500, budget.worstCaseMs());
    }

    @Test
    void rejectsWhenWorstCaseExceedsInterval() {
        // 3000ms x 3회 = 9000ms > 5000ms
        assertThrows(IllegalArgumentException.class,
                () -> new TimeoutBudget(5000, 1000, 3000, 3));
    }

    @Test
    void rejectsWhenWorstCaseEqualsInterval() {
        // 딱 맞으면 다음 사이클과 겹치므로 거부한다
        assertThrows(IllegalArgumentException.class,
                () -> new TimeoutBudget(4500, 1000, 1500, 3));
    }

    @Test
    void rejectsWhenCallTimeoutNotGreaterThanProbeTimeout() {
        assertThrows(IllegalArgumentException.class,
                () -> new TimeoutBudget(5000, 1000, 1000, 3));
    }
}
