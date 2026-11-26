package dev.workhard.dockerps;

import java.util.HashMap;
import java.util.Map;

/**
 * `docker ps -a --format '{{.Names}}\t{{.Status}}'` 출력을 이름 -> 정상 여부로 바꾼다.
 */
public final class ContainerStatusParser {

    private ContainerStatusParser() {
    }

    public static Map<String, Boolean> parse(String output) {
        Map<String, Boolean> result = new HashMap<>();
        for (String line : output.lines().toList()) { // CRLF도 처리
            String[] parts = line.split("\t", 2);      // 상태 문장 안 공백은 유지
            if (parts.length < 2) {
                continue;                              // 빈 줄/깨진 줄 무시
            }
            result.put(parts[0].trim(), isUp(parts[1].trim()));
        }
        return result;
    }

    /** Up으로 시작해도 unhealthy/Paused면 비정상. */
    public static boolean isUp(String status) {
        return status.startsWith("Up")
                && !status.contains("(unhealthy)")
                && !status.contains("(Paused)");
    }

    /** 목록에 없는 이름은 장애로 본다(삭제됐거나 이름이 틀린 경우도 정상으로 오판하지 않기). */
    public static boolean isHealthy(Map<String, Boolean> states, String name) {
        return states.getOrDefault(name, false);
    }
}
