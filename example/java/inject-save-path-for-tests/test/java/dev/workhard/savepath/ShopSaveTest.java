package dev.workhard.savepath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ShopSaveTest {

    @Test
    void autosaveWritesOnlyToInjectedPath(@TempDir Path dir) throws Exception {
        Path real = dir.resolve("real-save.txt");
        Files.writeString(real, "original"); // 사용자의 실제 저장 파일 역할
        Path scratch = dir.resolve("test-save.txt");

        new Shop(new SaveStore(scratch), true).openForBusiness();

        assertEquals("stock=100", Files.readString(scratch));
        assertEquals("original", Files.readString(real)); // 실제 파일은 그대로
    }

    @Test
    void autosaveOffWritesNothing(@TempDir Path dir) throws Exception {
        Path scratch = dir.resolve("test-save.txt");

        new Shop(new SaveStore(scratch), false).openForBusiness();

        assertEquals(false, Files.exists(scratch));
    }
}
