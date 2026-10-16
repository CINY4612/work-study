package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChannelResolverTest {

    @Test
    void oldStyleFallsThroughToNextCase() {
        // type 1인데 email까지 들어온다
        assertEquals(Set.of("push", "email"), ChannelResolver.oldStyle("1").keySet());
    }

    @Test
    void arrowStyleDoesNotFallThrough() {
        // 키 집합 전체를 비교해야 불필요한 값이 섞인 것을 잡는다
        assertEquals(Map.of("push", "push-url"), ChannelResolver.arrowStyle("1"));
        assertEquals(Map.of("email", "email-url"), ChannelResolver.arrowStyle("2"));
    }

    @Test
    void unknownTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ChannelResolver.arrowStyle("9"));
    }
}
