<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [docker, health-check, parsing, string]
date: 2026-11-26
example: example/java/docker-ps-status-parse
status: auto
-->

# docker ps 출력으로 컨테이너 생존 판정

> [!NOTE]
> `docker ps -a --format`으로 이름과 Status를 뽑고, Status가 `Up`으로 시작하고 `(unhealthy)`·`(Paused)`가 없을 때만 정상으로 본다.

> [!WARNING]
> **헷갈린 점** · `-a`를 빼면 죽은 컨테이너는 목록에서 사라진다. "목록에 없음"도 장애로 처리해야 한다.

## 📖 자세히

### 왜 이렇게 판정할까
`docker ps`의 Status 열은 컨테이너 상태를 사람이 읽는 문장으로 보여 준다. HTTP 포트를 두드리기 어려운 대상(WebSocket 서버, 포트를 열지 않는 데몬)은 이 문장만 봐도 "프로세스가 떠 있는가"를 싸게 확인할 수 있다. 단, 앱이 떠 있어도 내부가 고장 난 경우는 못 잡는다. 프로세스 생존만 보는 방식이라는 한계를 알고 써야 한다.

1. `-a` 옵션: 종료된 컨테이너(`Exited`)까지 출력한다. 빼면 죽은 컨테이너가 목록에서 아예 사라져 "죽음"과 "원래 없음"을 구분하기 어렵다.
2. `--format '{{.Names}}\t{{.Status}}'`: Go 템플릿이라 `\t`가 탭 문자가 된다. 한 줄에 `이름<TAB>상태`가 나오므로 탭으로 쪼개면 된다.
3. 판정: `Up`으로 시작하면 실행 중이지만, 뒤에 `(unhealthy)`나 `(Paused)`가 붙으면 비정상으로 본다. `Exited`, `Restarting`, `Created`, 목록에 없음은 모두 장애다.

```text
web-1\tUp 3 hours
worker-1\tExited (1) 2 minutes ago
cache-1\tRestarting (1) 5 seconds ago
api-1\tUp 10 minutes (unhealthy)
```

### 파싱 코드
```java
static Map<String, Boolean> parse(String output) {
    Map<String, Boolean> result = new HashMap<>();
    for (String line : output.lines().toList()) {   // CRLF(윈도우)도 lines()가 처리
        String[] parts = line.split("\t", 2);        // 상태 문장 안의 공백은 건드리지 않도록 2개로만 분리
        if (parts.length < 2) continue;              // 빈 줄·깨진 줄은 무시
        result.put(parts[0].trim(), isUp(parts[1].trim()));
    }
    return result;
}

static boolean isUp(String status) {
    return status.startsWith("Up")
        && !status.contains("(unhealthy)")
        && !status.contains("(Paused)");
}
```

### 목록에 없는 컨테이너
찾는 이름이 결과 맵에 없으면 `getOrDefault(name, false)`로 장애 처리한다. 이름이 오타이거나 컨테이너가 삭제된 경우에도 "정상"으로 오판하지 않기 위해서다.

### 주의점
- 실행하는 계정에 docker 권한이 필요하다. 컨테이너 안에서 돌리면 docker 소켓을 마운트해야 한다.
- `Up`은 프로세스가 살아 있다는 뜻일 뿐, 요청을 처리할 수 있다는 보장이 아니다. 컨테이너에 HEALTHCHECK가 있으면 `(healthy)`/`(unhealthy)`가 붙으니 활용한다.
- Status 문구는 사람용 문장이다. 정확한 판정이 필요하면 `docker inspect`의 `State.Status`/`State.Health.Status` 같은 구조화된 값을 쓰는 편이 안전하다.

## 🔎 더 공부할 것

- docker inspect --format - Status 문장 대신 구조화된 상태값을 읽을 수 있다
- Docker HEALTHCHECK - 컨테이너 안에서 앱 상태를 직접 점검하게 하는 방법
- ProcessBuilder - 자바에서 외부 명령을 실행하고 출력을 읽는 방법, 종료 코드 확인

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#docker` `#health-check` `#parsing` `#string` | 2026-11-26 | [예제 보기](../../example/java/docker-ps-status-parse) |
