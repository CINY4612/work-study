package dev.workhard;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignmentService {

    private final JdbcTemplate jdbc;

    public AssignmentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 기본 동작: RuntimeException이 나가면 프록시가 롤백한다 -> delete가 사라진다
    @Transactional
    public void cleanupAndFail(long nodeId) {
        jdbc.update("delete from assignment where node_id = ?", nodeId);
        throw new IllegalStateException("node down");
    }

    // noRollbackFor: 이 예외는 롤백 대상에서 제외 -> delete가 커밋된다
    @Transactional(noRollbackFor = IllegalStateException.class)
    public void cleanupAndFailKeep(long nodeId) {
        jdbc.update("delete from assignment where node_id = ?", nodeId);
        throw new IllegalStateException("node down");
    }
}
