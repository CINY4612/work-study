package dev.workhard;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public interface Notifier {
        void send(String message);
    }

    private final Notifier notifier;
    private final List<String> log = new ArrayList<>();

    public OrderService(Notifier notifier) {
        this.notifier = notifier;
    }

    // 부가 처리 실패를 삼키는 버전
    public String place(String id) {
        String result = "PLACED:" + id;
        try {
            notifier.send("placed " + id);
        } catch (RuntimeException e) {
            log.add("notify failed: " + e.getMessage());
        }
        return result;
    }

    // 비교용: 부가 처리 예외가 그대로 호출자에게 전달된다
    public String placeStrict(String id) {
        String result = "PLACED:" + id;
        notifier.send("placed " + id);
        return result;
    }

    public List<String> log() {
        return log;
    }
}
