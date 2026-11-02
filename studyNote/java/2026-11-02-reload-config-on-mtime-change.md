<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [java, config, lastModifiedTime, reload]
date: 2026-11-02
example: example/java/reload-config-on-mtime-change
status: auto
-->

# 파일 수정시각이 바뀔 때만 설정 다시 읽기

> [!NOTE]
> 조회할 때마다 파일의 수정시각만 비교하고 바뀐 경우에만 다시 파싱하면 재기동 없이 설정이 반영된다.

> [!WARNING]
> **헷갈린 점** · 매번 읽으면 낭비이고, 시작 시 한 번만 읽으면 변경을 놓친다. 수정시각 비교가 둘 사이의 값싼 절충이다.

## 📖 자세히

### 왜 이렇게 동작할까
수정시각(`Files.getLastModifiedTime`)은 파일 내용을 읽지 않고 메타데이터만 조회하므로 가볍다. 이전에 읽은 시각과 같으면 캐시를 쓰고, 다르면 다시 파싱한다. 여러 스레드가 조회할 수 있으므로 `synchronized`로 감싸 한 번만 다시 읽게 한다.

```java
public synchronized Properties get() throws IOException {
    FileTime now = Files.getLastModifiedTime(path); // 내용이 아닌 메타데이터만 조회
    if (cached == null || !now.equals(lastModified)) {
        Properties p = new Properties();
        try (var in = Files.newInputStream(path)) { p.load(in); }
        cached = p;           // 파싱이 끝난 뒤에 교체
        lastModified = now;
        loadCount++;
    }
    return cached;
}
```

### 주의점
- 수정시각 해상도가 낮은 파일시스템에서는 같은 초 안의 두 번 수정을 놓칠 수 있다.
- 파싱에 실패했을 때 이전 값을 유지할지 정해 둔다. 위 코드는 예외를 던지고 기존 캐시는 건드리지 않는다.

## 🔎 더 공부할 것

- WatchService - 폴링 대신 파일 변경 이벤트로 감지
- @RefreshScope - Spring Cloud의 설정 재로딩
- 내용 해시 비교 - 수정시각 해상도 문제를 피하는 방법

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#java` `#config` `#lastModifiedTime` `#reload` | 2026-11-02 | [예제 보기](../../example/java/reload-config-on-mtime-change) |
