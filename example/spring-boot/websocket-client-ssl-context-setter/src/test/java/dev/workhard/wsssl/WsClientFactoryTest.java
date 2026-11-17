package dev.workhard.wsssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

class WsClientFactoryTest {

    @Test
    void setterKeepsSslContextAsTypedProperty() throws Exception {
        SSLContext ctx = WsClientFactory.trustAll();
        StandardWebSocketClient client = WsClientFactory.withSetter(ctx);
        // setter는 타입이 있는 필드에 보관하고, 핸드셰이크 때 Spring이 구현체에 맞게 전달한다
        assertSame(ctx, client.getSslContext());
        // 문자열 키 Map은 건드리지 않는다
        assertTrue(client.getUserProperties().isEmpty());
    }

    @Test
    void userPropertyApproachJustStoresAnOpaqueEntry() throws Exception {
        SSLContext ctx = WsClientFactory.trustAll();
        StandardWebSocketClient client = WsClientFactory.withUserProperty(ctx);
        // 문자열 키는 그냥 Map 항목일 뿐, 구현체가 읽는지는 컴파일러가 보장하지 않는다
        assertEquals(1, client.getUserProperties().size());
        assertTrue(client.getUserProperties().containsKey("org.apache.tomcat.websocket.SSL_CONTEXT"));
    }
}
