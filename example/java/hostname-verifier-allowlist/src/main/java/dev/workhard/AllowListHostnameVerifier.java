package dev.workhard;

import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

// 테스트 환경에서만 허용할 호스트를 명시한다. 전부 true를 돌려주는 검증은 피한다.
public class AllowListHostnameVerifier implements HostnameVerifier {
    private final Set<String> allowed;

    public AllowListHostnameVerifier(Set<String> allowed) {
        this.allowed = Set.copyOf(allowed);
    }

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return allowed.contains(hostname); // 목록에 없으면 거부
    }
}
