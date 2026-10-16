package dev.workhard;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChannelResolver {

    // 잘못된 예: "1"에 break가 없어서 "2"의 email까지 같이 담긴다
    public static Map<String, String> oldStyle(String type) {
        Map<String, String> r = new LinkedHashMap<>();
        switch (type) {
            case "1":
                r.put("push", "push-url");
                // break 누락
            case "2":
                r.put("email", "email-url");
                break;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
        return r;
    }

    // 올바른 예: 화살표 switch는 fall-through가 없다
    public static Map<String, String> arrowStyle(String type) {
        Map<String, String> r = new LinkedHashMap<>();
        switch (type) {
            case "1" -> r.put("push", "push-url");
            case "2" -> r.put("email", "email-url");
            default -> throw new IllegalArgumentException("unknown type: " + type);
        }
        return r;
    }
}
