package dev.workhard.specinherit;

import java.util.Map;

public class SpecResolver {
    private final Map<String, Spec> specs;

    public SpecResolver(Map<String, Spec> specs) {
        this.specs = specs;
    }

    public Spec resolve(String id) {
        Spec own = specs.get(id);
        if (own != null) {
            return own; // 자기 설정이 있으면 그대로 쓴다
        }
        // 같은 종류의 01번을 기준으로 삼는다 (예: BOOK02 -> BOOK01)
        String baseId = id.substring(0, id.length() - 2) + "01";
        Spec base = specs.get(baseId);
        if (base == null) {
            throw new IllegalArgumentException("no base spec for " + id);
        }
        // 공통 값(shape)만 물려받고 고유 값(sheet)은 비워 둔다
        return new Spec(base.shape(), null);
    }
}
