package dev.workhard.configname;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// 지정하지 않으면 application.* 만 찾으므로 shop-settings.yaml은 무시된다
@SpringBootTest
class WithoutConfigNameTest {
    @Autowired
    ShopInfo shopInfo;

    @Test
    void ignoresFileWithoutConfigName() {
        assertEquals("미설정", shopInfo.name());
    }
}
