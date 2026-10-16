package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.workhard.LoadRepository.Load;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class LoadRepositoryTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired LoadRepository repository;

    @BeforeEach
    void setUp() {
        jdbc.execute("DROP TABLE IF EXISTS room_node");
        jdbc.execute("DROP TABLE IF EXISTS ms_node");
        jdbc.execute("CREATE TABLE ms_node (server_id VARCHAR(20), daemon_id VARCHAR(20), status INT, PRIMARY KEY (server_id, daemon_id))");
        jdbc.execute("CREATE TABLE room_node (room_id VARCHAR(20) PRIMARY KEY, ms_server_id VARCHAR(20), daemon_id VARCHAR(20))");
        // daemon_id 'D1'이 서버 S1, S2에 모두 있다 (PK는 server_id + daemon_id)
        jdbc.update("INSERT INTO ms_node VALUES ('S1','D1',1), ('S1','D2',1), ('S2','D1',1)");
        // S1/D1: 2개, S1/D2: 0개, S2/D1: 1개
        jdbc.update("INSERT INTO room_node VALUES ('R1','S1','D1'), ('R2','S1','D1'), ('R3','S2','D1')");
    }

    @Test
    void leftJoinKeepsServerWithZeroRooms() {
        List<Load> loads = repository.loads();

        assertEquals(3, loads.size());
        assertEquals(new Load("S1", "D2", 0), repository.leastLoaded()); // 빈 서버가 선택된다
    }

    @Test
    void joinKeyIncludesServerIdSoSameDaemonIdDoesNotMix() {
        List<Load> loads = repository.loads();

        assertEquals(2, loads.stream().filter(l -> l.serverId().equals("S1") && l.daemonId().equals("D1")).findFirst().get().connCount());
        assertEquals(1, loads.stream().filter(l -> l.serverId().equals("S2")).findFirst().get().connCount());
    }

    @Test
    void innerJoinDropsServerWithZeroRooms() {
        List<Load> loads = repository.loadsInnerJoin();

        assertEquals(2, loads.size());
        assertFalse(loads.stream().anyMatch(l -> l.daemonId().equals("D2"))); // 빈 서버가 후보에서 빠진다
    }
}
