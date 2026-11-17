package dev.workhard.wsssl;

import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

public class WsClientFactory {

    // 모든 인증서를 믿는 SSLContext (로컬 개발 확인용, 운영 금지)
    static SSLContext trustAll() throws Exception {
        TrustManager[] tm = {new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a) {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }};
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tm, new SecureRandom());
        return ctx;
    }

    // 전용 setter로 지정: 타입이 있는 API라 지원이 사라지면 컴파일 단계에서 드러난다
    public static StandardWebSocketClient withSetter(SSLContext ctx) {
        StandardWebSocketClient client = new StandardWebSocketClient();
        client.setSslContext(ctx);
        return client;
    }

    // 문자열 키 방식: 구현체가 읽지 않으면 조용히 무시된다
    public static StandardWebSocketClient withUserProperty(SSLContext ctx) {
        StandardWebSocketClient client = new StandardWebSocketClient();
        client.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", ctx);
        return client;
    }
}
