package dev.workhard;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HealthProbe {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(1))
            .build();

    /** 응답이 오기만 하면 true. 상태코드는 보지 않는다 (프록시 뒤 장애를 놓친다) */
    public boolean isReachable(String url) {
        return status(url) > 0;
    }

    /** 502/503/504는 프록시가 대신 낸 응답일 수 있으므로 장애로 본다 */
    public boolean isHealthy(String url) {
        int status = status(url);
        return status > 0 && status != 502 && status != 503 && status != 504;
    }

    // 연결 실패/타임아웃이면 -1
    private int status(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(1)).GET().build();
            return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (IOException e) {
            return -1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            return -1;
        }
    }
}
