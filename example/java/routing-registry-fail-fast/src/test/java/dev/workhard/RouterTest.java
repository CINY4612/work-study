package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.workhard.Router.Destination;
import dev.workhard.Router.NoRouteException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RouterTest {

    private final Map<Destination, String> addresses = Map.of(
            Destination.CATALOG, "http://catalog.local",
            Destination.PAYMENT, "http://payment.local");

    @Test
    void resolvesRegisteredKeyToItsDestinationAddress() {
        Router router = new Router(addresses);
        router.register("book.list", Destination.CATALOG);
        router.register("order.pay", Destination.PAYMENT);

        assertEquals("http://payment.local", router.resolve("order.pay"));
    }

    @Test
    void missingAddressFailsInsteadOfFallingBack() {
        Router router = new Router(addresses);
        router.register("order.ship", Destination.SHIPPING); // SHIPPING 주소는 설정에 없음

        assertThrows(NoRouteException.class, () -> router.resolve("order.ship"));
    }

    @Test
    void unknownKeyFails() {
        Router router = new Router(addresses);
        assertThrows(NoRouteException.class, () -> router.resolve("nope"));
    }

    @Test
    void duplicateRegistrationIsRejected() {
        Router router = new Router(addresses);
        router.register("book.list", Destination.CATALOG);
        assertThrows(IllegalStateException.class,
                () -> router.register("book.list", Destination.PAYMENT));
    }
}
