package dev.workhard;

import java.util.HashMap;
import java.util.Map;

public class Router {

    // 목적지는 enum으로 고정: 오타가 컴파일 시점에 걸린다
    public enum Destination { CATALOG, PAYMENT, SHIPPING }

    public static class NoRouteException extends RuntimeException {
        public NoRouteException(String message) {
            super(message);
        }
    }

    private final Map<String, Destination> rules = new HashMap<>();
    private final Map<Destination, String> addresses;

    public Router(Map<Destination, String> addresses) {
        this.addresses = Map.copyOf(addresses);
    }

    public void register(String key, Destination destination) {
        // 같은 키를 두 번 등록하면 덮어쓰지 않고 바로 실패
        if (rules.putIfAbsent(key, destination) != null) {
            throw new IllegalStateException("duplicate rule: " + key);
        }
    }

    public String resolve(String key) {
        Destination destination = rules.get(key);
        if (destination == null) {
            throw new NoRouteException("no rule: " + key);
        }
        String address = addresses.get(destination);
        // 주소가 없으면 다른 목적지로 대체하지 않고 오류로 드러낸다
        if (address == null) {
            throw new NoRouteException("no address for " + destination);
        }
        return address;
    }
}
