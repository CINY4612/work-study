package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void sumsPriceByCategoryInKeyOrder() {
        var books = List.of(
                new App.Book("a", "life", 10), new App.Book("b", "dev", 5), new App.Book("c", "dev", 7));
        Map<String, Integer> result = App.totalPriceByCategory(books);
        assertEquals("[dev, life]", result.keySet().toString());
        assertEquals(12, result.get("dev"));
    }
}
