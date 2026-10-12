package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class RegistryTest {

    private final Registry.Legacy legacy = new Registry.Legacy();

    @Test
    void equalValuesHideTheSwap() {
        long centerId = 1, subsId = 1;
        // 순서를 바꿔 호출했지만 값이 같아서 결과가 똑같다 -> 버그가 숨는다
        assertEquals(legacy.register(subsId, centerId), legacy.register(centerId, subsId));
    }

    @Test
    void distinctValuesRevealTheSwap() {
        long centerId = 5, subsId = 7;
        // 값이 다르면 뒤바뀐 결과가 드러난다
        assertNotEquals(legacy.register(subsId, centerId), legacy.register(centerId, subsId));
    }

    @Test
    void typedArgumentsKeepTheirMeaning() {
        var typed = new Registry.Typed();
        // 순서를 바꾸면 컴파일되지 않으므로 항상 올바른 의미로 저장된다
        assertEquals("subs=7,center=5",
                typed.register(new Registry.CenterId(5), new Registry.SubsId(7)));
    }
}
