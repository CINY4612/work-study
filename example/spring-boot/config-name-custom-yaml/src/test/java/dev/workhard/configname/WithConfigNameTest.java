package dev.workhard.configname;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// config name을 지정하면 shop-settings.yaml을 읽는다
@SpringBootTest(properties = "spring.config.name=shop-settings")
class WithConfigNameTest {
    @Autowired
    ShopInfo shopInfo;

    @Test
    void readsFileWhenConfigNameIsGiven() {
        assertEquals("책방", shopInfo.name());
    }
}
