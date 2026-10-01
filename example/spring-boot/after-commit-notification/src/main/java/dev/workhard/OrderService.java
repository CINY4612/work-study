package dev.workhard;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OrderService {

    /** 외부 알림을 흉내 내는 클래스. 보낸 내용을 메모리에 쌓는다. */
    @Component
    public static class OrderNotifier {
        private final List<String> sent = new CopyOnWriteArrayList<>();

        public void send(String item) {
            sent.add(item);
        }

        public List<String> sent() {
            return sent;
        }
    }

    private final JdbcTemplate jdbc;
    private final OrderNotifier notifier;

    public OrderService(JdbcTemplate jdbc, OrderNotifier notifier) {
        this.jdbc = jdbc;
        this.notifier = notifier;
    }

    @Transactional
    public void place(String item, boolean fail) {
        jdbc.update("insert into orders(item) values (?)", item);

        // 바로 보내지 않고 '커밋 성공 후' 실행할 콜백으로 등록한다
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        notifier.send(item);
                    }
                });

        // 롤백되면 afterCommit은 호출되지 않는다
        if (fail) {
            throw new IllegalStateException("force rollback");
        }
    }
}
