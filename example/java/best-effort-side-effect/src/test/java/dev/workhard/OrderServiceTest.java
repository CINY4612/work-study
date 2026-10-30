package dev.workhard;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private final OrderService.Notifier failing = m -> {
        throw new IllegalStateException("down");
    };

    @Test
    void sideEffectFailureDoesNotFailMainResult() {
        OrderService service = new OrderService(failing);
        assertEquals("PLACED:1", service.place("1"));
        assertEquals(1, service.log().size()); // 로그는 남는다
    }

    @Test
    void strictVersionPropagatesFailure() {
        OrderService service = new OrderService(failing);
        assertThrows(IllegalStateException.class, () -> service.placeStrict("1"));
    }
}
