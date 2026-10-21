package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HealthProbeTest {

    private HttpServer server;
    private String base;
    private final HealthProbe probe = new HealthProbe();

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/ok", ex -> { ex.sendResponseHeaders(200, -1); ex.close(); });
        server.createContext("/bad-gateway", ex -> { ex.sendResponseHeaders(502, -1); ex.close(); });
        server.start();
        base = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void okResponseIsHealthy() {
        assertTrue(probe.isReachable(base + "/ok"));
        assertTrue(probe.isHealthy(base + "/ok"));
    }

    @Test
    void badGatewayLooksAliveWhenOnlyReachabilityIsChecked() {
        assertTrue(probe.isReachable(base + "/bad-gateway")); // 함정: 502인데 살아있다고 판정
        assertFalse(probe.isHealthy(base + "/bad-gateway"));
    }

    @Test
    void connectionRefusedIsDownForBoth() {
        String url = base + "/ok";
        server.stop(0); // 포트를 닫아 연결 거부 상태로 만든다
        assertFalse(probe.isReachable(url));
        assertFalse(probe.isHealthy(url));
    }
}
