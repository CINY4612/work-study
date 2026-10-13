package dev.workhard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

// DB 테이블을 흉내 낸 인메모리 저장소. 실제 키는 (roomId, nodeId)라고 가정한다.
public class RoomRegistry {

    public record Row(String roomId, String nodeId, long seq) {}

    private final List<Row> rows = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong();

    // 삭제 단계: 지운 행 수를 돌려준다.
    public int deleteByRoom(String roomId) {
        int before = rows.size();
        rows.removeIf(r -> r.roomId().equals(roomId));
        return before - rows.size();
    }

    // 삽입 단계: roomId만으로는 중복을 막지 않는다 (PK에 nodeId가 있기 때문).
    public void insert(String roomId, String nodeId) {
        rows.add(new Row(roomId, nodeId, seq.incrementAndGet()));
    }

    // 한 요청 안에서는 삭제 -> 삽입이 이어진다.
    public void register(String roomId, String nodeId) {
        deleteByRoom(roomId);
        insert(roomId, nodeId);
    }

    public long count(String roomId) {
        return rows.stream().filter(r -> r.roomId().equals(roomId)).count();
    }

    // 방어적 조회: 여러 건이어도 예외 없이 가장 늦게 등록된 노드를 고른다.
    public Optional<String> findNode(String roomId) {
        return rows.stream()
                .filter(r -> r.roomId().equals(roomId))
                .max(Comparator.comparingLong(Row::seq))
                .map(Row::nodeId);
    }
}
