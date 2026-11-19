package dev.workhard.savepath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** 저장 경로를 생성자로 받는 단순 파일 저장소. */
public class SaveStore {
    private final Path file;

    public SaveStore(Path file) {
        this.file = file; // 경로를 밖에서 주입 -> 테스트는 임시 경로를 넘길 수 있다
    }

    public void write(String content) throws IOException {
        Files.writeString(file, content);
    }

    public String read() throws IOException {
        return Files.exists(file) ? Files.readString(file) : "";
    }
}
