package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class GenreCodeMapperTest {

    private final GenreCodeMapper mapper =
            new GenreCodeMapper(Map.of("01", "FICTION", "02", "COMIC"));

    @Test
    void mappedExternalCodeBecomesInternalCode() {
        assertEquals("FICTION", mapper.toInternal("01"));
    }

    @Test
    void lenientPassesUnknownValueThrough() {
        // 이미 내부 코드를 보낸 경우에도 그대로 통과한다
        assertEquals("COMIC", mapper.toInternal("COMIC"));
    }

    @Test
    void strictRejectsUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> mapper.toInternalStrict("99"));
    }
}
