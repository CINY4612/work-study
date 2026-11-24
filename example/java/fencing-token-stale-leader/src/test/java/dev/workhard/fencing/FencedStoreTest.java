package dev.workhard.fencing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FencedStoreTest {
    @Test
    void staleLeaderWriteIsRejected() {
        TokenIssuer issuer = new TokenIssuer();
        FencedStore store = new FencedStore();

        long tokenA = issuer.next(); // 리더 A가 락을 얻음
        long tokenB = issuer.next(); // A가 멈춘 사이 B가 새 리더가 됨

        assertTrue(store.write(tokenB, "B의 값"));
        // A가 깨어나 뒤늦게 쓰려 해도 토큰이 작아서 거부된다
        assertFalse(store.write(tokenA, "A의 값"));
        assertEquals("B의 값", store.value());
    }

    @Test
    void sameLeaderCanWriteRepeatedly() {
        FencedStore store = new FencedStore();
        assertTrue(store.write(1, "첫 값"));
        assertTrue(store.write(1, "둘째 값"));
        assertEquals("둘째 값", store.value());
    }

    @Test
    void newerTokenOverridesOlderValue() {
        FencedStore store = new FencedStore();
        store.write(1, "old");
        assertTrue(store.write(2, "new"));
        assertEquals("new", store.value());
    }
}
