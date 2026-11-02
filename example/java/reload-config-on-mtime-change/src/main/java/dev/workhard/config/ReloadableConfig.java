package dev.workhard.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Properties;

public class ReloadableConfig {
    private final Path path;
    private FileTime lastModified;
    private Properties cached;
    private int loadCount;

    public ReloadableConfig(Path path) {
        this.path = path;
    }

    // 여러 스레드가 호출해도 다시 읽기는 한 번만 일어나도록 synchronized
    public synchronized Properties get() throws IOException {
        FileTime now = Files.getLastModifiedTime(path); // 내용은 읽지 않는 값싼 조회
        if (cached == null || !now.equals(lastModified)) {
            Properties p = new Properties();
            try (var in = Files.newInputStream(path)) {
                p.load(in);
            }
            cached = p; // 파싱 성공 후에만 교체
            lastModified = now;
            loadCount++;
        }
        return cached;
    }

    public int loadCount() {
        return loadCount;
    }
}
