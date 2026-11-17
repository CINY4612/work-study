package dev.workhard.cleanup;

import java.util.Set;
import java.util.TreeMap;

/** 여러 노드가 공유하는 키-값 저장소(인메모리). */
public class KeyValueStore {
    private final TreeMap<String, String> data = new TreeMap<>();

    public void put(String key, String value) {
        data.put(key, value);
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    public void deleteByPrefix(String prefix) {
        data.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public Set<String> keys() {
        return data.keySet();
    }
}
