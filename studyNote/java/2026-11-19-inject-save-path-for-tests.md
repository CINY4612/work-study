<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [testing, tempdir, side-effect, dependency-injection]
date: 2026-11-19
example: example/java/inject-save-path-for-tests
status: auto
-->

# 테스트가 실제 저장 파일을 덮어쓰지 않게 하기

> [!NOTE]
> 저장 경로를 생성자로 주입하고 테스트는 @TempDir 경로만 쓰게 하면, 자동 저장이 실제 저장 파일을 건드리지 않는다.

> [!WARNING]
> **헷갈린 점** · 테스트가 '영업 시작'만 호출해도 내부 자동 저장이 돌아 실제 세이브를 덮어쓸 수 있다는 점이 헷갈린다.

## 📖 자세히

### 왜 이런 일이 생길까
테스트 대상 메서드(예: 하루 시작)가 **부수 효과로 파일을 쓰는** 경우가 있다. 저장 경로가 코드 안에 고정(하드코딩)되어 있으면 테스트도 같은 경로에 쓰게 되고, 개발 PC의 실제 데이터가 테스트 상태로 바뀐다. 겉으로는 '저장 기능을 테스트하지 않았는데' 저장 기능이 실행된 것이다.

### 해결: 경로(의존성)를 밖에서 넣는다
- 저장소 클래스가 `Path`를 생성자로 받게 한다. 운영 코드는 실제 경로, 테스트는 임시 경로를 넘긴다.
- JUnit 5의 `@TempDir`은 테스트마다 임시 폴더를 만들고 끝나면 지워 준다.
- 자동 저장이 필요 없는 테스트를 위해 `autoSave` 같은 스위치도 둘 수 있다.

```java
public class SaveStore {
    private final Path file;
    public SaveStore(Path file) { this.file = file; } // 경로를 주입받는다
    public void write(String s) throws IOException { Files.writeString(file, s); }
}

@Test
void autosaveGoesToInjectedPath(@TempDir Path dir) throws Exception {
    Path real = dir.resolve("real-save.json");
    Files.writeString(real, "original");
    Path scratch = dir.resolve("test-save.json");
    new Shop(new SaveStore(scratch), true).openForBusiness();
    assertEquals("original", Files.readString(real)); // 실제 파일은 그대로
}
```

### 주의점
- 경로를 주입하지 못하는 환경(엔진/프레임워크)이라면 테스트 전에 저장 파일을 백업하고, 자동 저장을 끄는 옵션을 쓴다.
- '이 테스트가 어떤 파일을 쓰는가'를 테스트 안에서 단언(assert)으로 확인하면 회귀를 막을 수 있다.

## 🔎 더 공부할 것

- @TempDir - 임시 폴더 생명주기와 필드/파라미터 주입 차이
- 테스트 더블 - 파일 대신 인메모리 저장소로 바꾸는 방법
- 부수 효과 분리 - 상태 변경과 저장을 나누는 설계

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#testing` `#tempdir` `#side-effect` `#dependency-injection` | 2026-11-19 | [예제 보기](../../example/java/inject-save-path-for-tests) |
