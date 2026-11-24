package dev.workhard.configname;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShopInfo {
    // 값이 없으면 "미설정" - 파일을 못 읽어도 에러가 나지 않는다
    @Value("${shop.name:미설정}")
    private String name;

    public String name() {
        return name;
    }
}
