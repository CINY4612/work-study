package dev.workhard.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReloadableConfigTest {

    @TempDir
    Path dir;

    @Test
    void readsOnlyOnceWhileUnchanged() throws Exception {
        Path file = dir.resolve("app.properties");
        Files.writeString(file, "shelf=A\n");
        ReloadableConfig config = new ReloadableConfig(file);

        config.get();
        config.get();

        assertEquals(1, config.loadCount()); // 두 번 조회해도 파싱은 한 번
    }

    @Test
    void reloadsWhenModifiedTimeChanges() throws Exception {
        Path file = dir.resolve("app.properties");
        Files.writeString(file, "shelf=A\n");
        ReloadableConfig config = new ReloadableConfig(file);
        assertEquals("A", config.get().getProperty("shelf"));

        Files.writeString(file, "shelf=B\n");
        // 해상도 문제를 피하려고 수정시각을 확실히 미래로 지정
        Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis() + 5000));

        assertEquals("B", config.get().getProperty("shelf"));
        assertEquals(2, config.loadCount());
    }
}
