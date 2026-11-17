package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ServiceResolverTest {

    private final ServiceResolver resolver = new ServiceResolver(Map.of(
            "943", List.of("1", "2"),
            "944", List.of()));

    @Test
    void correctKeyReturnsStoredServices() {
        assertEquals(List.of("1", "2"), resolver.resolveStrict("943"));
    }

    @Test
    void naiveSilentlyFallsBackWhenKeyIsWrong() {
        // 다른 식별자(loginId)를 넘겨도 오류 없이 기본값이 나온다 -> 원인이 가려짐
        assertEquals(List.of("1"), resolver.resolveNaive("login-abc"));
    }

    @Test
    void strictFailsFastOnWrongKey() {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolveStrict("login-abc"));
    }

    @Test
    void strictUsesDefaultOnlyForRegisteredEmptyList() {
        assertEquals(List.of("1"), resolver.resolveStrict("944"));
    }
}
