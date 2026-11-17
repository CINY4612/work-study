package dev.workhard.envelope;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;

// 이전 방식: extra의 키가 resultCode와 같은 층에 펼쳐진다
public record FlatResponse(int resultCode, String resultMsg, @JsonIgnore Map<String, Object> extra) {
    // any-getter는 한 곳에만 둔다. 컴포넌트에도 붙이면 "Multiple 'any-getter'" 오류가 난다
    @JsonAnyGetter
    public Map<String, Object> any() {
        return extra;
    }
}
