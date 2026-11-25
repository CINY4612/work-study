package dev.workhard.replyrouting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReplyBusTest {
    @Test
    void distinctIdsReceiveOnlyTheirOwnReply() {
        ReplyBus bus = new ReplyBus(false);
        List<String> one = new ArrayList<>();
        List<String> two = new ArrayList<>();
        bus.register("1", one::add);
        bus.register("2", two::add);

        assertEquals(1, bus.reply("2", "order-ok"));
        assertTrue(one.isEmpty());
        assertEquals(List.of("order-ok"), two);
    }

    @Test
    void sameIdMakesBothInstancesReceiveTheReply() {
        ReplyBus bus = new ReplyBus(false);
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        bus.register("1", first::add);
        bus.register("1", second::add); // 설정을 복사해 ID가 같아진 경우

        assertEquals(2, bus.reply("1", "order-ok")); // 한 응답이 두 곳에 간다
        assertEquals(1, first.size());
        assertEquals(1, second.size());
    }

    @Test
    void strictBusRejectsDuplicateIdAtRegistration() {
        ReplyBus bus = new ReplyBus(true);
        bus.register("1", m -> {});
        assertThrows(IllegalStateException.class, () -> bus.register("1", m -> {}));
    }
}
