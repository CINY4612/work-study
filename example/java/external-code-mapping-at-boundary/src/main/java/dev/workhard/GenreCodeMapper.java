package dev.workhard;

import java.util.Map;

/** 외부 장르 코드("01" 등)를 내부 코드("FICTION" 등)로 바꾸는 경계용 변환기. */
public record GenreCodeMapper(Map<String, String> mapping) {

    // lenient: 매핑에 없으면 입력값을 그대로 내부 코드로 본다
    public String toInternal(String external) {
        return mapping.getOrDefault(external, external);
    }

    // strict: 매핑에 없으면 입구에서 바로 실패시킨다
    public String toInternalStrict(String external) {
        String code = mapping.get(external);
        if (code == null) {
            throw new IllegalArgumentException("unknown genre: " + external);
        }
        return code;
    }
}
