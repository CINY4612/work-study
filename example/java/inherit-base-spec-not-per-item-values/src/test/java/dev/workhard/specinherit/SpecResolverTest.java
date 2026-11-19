package dev.workhard.specinherit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SpecResolverTest {
    private final SpecResolver resolver = new SpecResolver(Map.of(
            "BOOK01", new Spec("prism", "0,0,40,60"),
            "CUPS01", new Spec("box", null),
            "CUPS03", new Spec("cylinder", "1,1,2,2")));

    @Test
    void missing_item_inherits_shape_but_not_sheet() {
        Spec s = resolver.resolve("BOOK02");
        assertEquals("prism", s.shape());
        assertNull(s.sheet()); // 좌표는 항목마다 달라서 물려받지 않는다
    }

    @Test
    void own_spec_wins_over_base() {
        assertEquals(new Spec("cylinder", "1,1,2,2"), resolver.resolve("CUPS03"));
    }

    @Test
    void no_base_spec_fails_fast() {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("PENS02"));
    }
}
