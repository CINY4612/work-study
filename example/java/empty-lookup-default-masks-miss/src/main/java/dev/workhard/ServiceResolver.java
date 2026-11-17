package dev.workhard;

import java.util.List;
import java.util.Map;

public class ServiceResolver {

    public static final String DEFAULT_SERVICE = "1";

    // key = 계정 id, value = 가진 서비스 목록
    private final Map<String, List<String>> servicesByAccountId;

    public ServiceResolver(Map<String, List<String>> servicesByAccountId) {
        this.servicesByAccountId = servicesByAccountId;
    }

    // 미스(키 틀림)와 빈 결과를 구분하지 못한다
    public List<String> resolveNaive(String key) {
        List<String> s = servicesByAccountId.getOrDefault(key, List.of());
        return s.isEmpty() ? List.of(DEFAULT_SERVICE) : s;
    }

    // 미스는 예외, 등록된 빈 목록만 기본값으로 대체한다
    public List<String> resolveStrict(String key) {
        List<String> s = servicesByAccountId.get(key);
        if (s == null) {
            throw new IllegalArgumentException("unknown account: " + key);
        }
        return s.isEmpty() ? List.of(DEFAULT_SERVICE) : s;
    }
}
