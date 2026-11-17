<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [replication, failover, split-brain, read-your-writes]
date: 2026-11-17
example: example/java/replication-lag-and-failover-basics
status: auto
-->

# Primary-Replica 복제와 장애 전환 개념 정리

> [!NOTE]
> Primary-Replica는 비동기 복제라 Replica 읽기는 방금 쓴 값을 못 볼 수 있고, 승격 시 마지막 커밋이 사라질 수 있다.

> [!WARNING]
> **헷갈린 점** · 서버 2대에서는 '상대가 죽음'과 '연결만 끊김'을 구분 못 해 자동 승격이 split-brain(Primary 둘)을 만들 수 있다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
- **Primary-Replica 복제**: 쓰기는 Primary 한 곳이 받고, 그 변경 기록(복제 로그)을 Replica가 따라 적용하는 구조다.
- 필요한 이유: Primary가 죽었을 때 Replica를 새 Primary로 올려(장애 전환, failover) 서비스를 이어 가기 위해서다.
- 대신 "복제가 언제 반영되는가"와 "누가 Primary인가"라는 새 문제가 생긴다.

### 동작 원리
1. 클라이언트가 Primary에 쓴다. Primary는 값을 저장하고 복제 로그에 기록한다.
2. **비동기 복제**에서는 Primary가 Replica 반영을 기다리지 않고 클라이언트에 커밋 성공을 돌려준다.
3. Replica는 복제 로그를 조금 늦게 따라 적용한다. 이 시간 차이가 **복제 지연(lag)** 이다.
4. Primary가 죽으면 모니터(예: 프록시/관리 도구)가 Replica를 승격한다. 아직 적용되지 않은 로그는 사라진다.

```text
client --write--> Primary --(복제 로그, 늦게)--> Replica
       <--OK-----   (Replica 반영 전에 OK 반환)
Primary 사망 -> Replica 승격 -> 반영 안 된 마지막 쓰기 유실
```

### 구성 요소/용어 정리
| 용어 | 뜻 |
|---|---|
| 복제 지연 | Primary 쓰기가 Replica에 보이기까지의 시간 |
| read-your-writes | 내가 방금 쓴 값을 내가 바로 읽을 수 있다는 보장 |
| 읽기/쓰기 분리 | SELECT는 Replica, 쓰기는 Primary로 자동 분배하는 방식 |
| 장애 전환(failover) | Replica를 새 Primary로 올리는 일 |
| split-brain | 연결 단절로 양쪽이 서로 "내가 Primary"라고 믿는 상태 |
| 정족수(quorum) | 승격 같은 결정에 필요한 최소 동의 수 |

### 코드로 보기
```java
// 쓰기는 Primary에만 반영하고, 복제 로그에 쌓아 둔다 (비동기 복제 흉내)
public void write(String key, String value) {
    primary.put(key, value);
    replicationLog.add(new String[] {key, value});
}

// Replica는 로그를 적용한 만큼만 안다
public Optional<String> readFromReplica(String key) {
    return Optional.ofNullable(replica.get(key));
}

// 승격: Replica에 있던 데이터만 새 Primary로 넘어간다 (로그에 남은 건 유실)
public ReplicatedStore failover() {
    return new ReplicatedStore(replica);
}
```

```java
// 서버 2대에서 정족수 판단: 각자 '내가 본 서버' 기준이냐, '전체 서버' 기준이냐
public static boolean mayPromote(Mode mode, int seenRunning, int total) {
    return switch (mode) {
        case MAJORITY_OF_RUNNING -> seenRunning >= 1; // 단절 시 양쪽 다 true -> split-brain
        case MAJORITY_OF_SERVERS -> seenRunning > total / 2; // 단절 시 양쪽 다 false
    };
}
```

### 비교
| 선택 | 얻는 것 | 잃는 것 |
|---|---|---|
| 비동기 복제 | 쓰기가 빠르다 | 승격 시 마지막 커밋 유실 가능 |
| 반동기(semi-sync) 복제 | 최소 1개 Replica 수신 확인 후 커밋, 유실 위험 감소 | 쓰기 지연 증가 |
| 정족수 = 보이는 서버 과반 | 서버 하나가 죽어도 자동 승격 | 단절 시 split-brain |
| 정족수 = 전체 서버 과반 | split-brain 방지 | 2대 중 1대가 죽으면 자동 승격 불가 |

### 자주 하는 실수와 주의점
- 쓰고 바로 읽는 코드(등록 직후 조회)를 읽기/쓰기 분리 뒤에 두면 Replica에서 못 찾을 수 있다. `readOnly` 트랜잭션도 Replica로 갈 수 있다.
  - 대책: 그 조회만 Primary로 가는 전용 연결을 쓰거나, 복제 반영을 기다리는 옵션(causal read 등)을 쓴다.
- "자동 승격"과 "split-brain 방지"는 서버 2대만으로는 동시에 얻기 어렵다. 제3의 판정자(감시 노드)나 이중화된 링크가 필요하다.
- 갈라진 두 Primary는 자동으로 합쳐지지 않는다. 사람이 한쪽 데이터를 버리거나 수동 병합해야 한다.

### 세션에서 나온 질문 정리
- Q. Replica에서 읽으면 왜 방금 등록한 데이터가 없나?
  - A. 복제가 비동기라 아직 Replica에 적용되기 전일 수 있다. Primary 읽기를 강제해야 한다.
- Q. 자동 승격이면 데이터는 안전한가?
  - A. 아니다. 반영 안 된 마지막 쓰기는 사라진다. 반동기 복제로 위험을 줄인다.
- Q. 서버 사이 선만 끊기면 어떻게 되나?
  - A. 양쪽이 상대를 죽었다고 판단해 각자 승격하면 Primary가 둘이 된다. 정족수 기준을 전체 서버 과반으로 바꾸면 막지만 서버 한 대 장애 시 자동 승격도 못 한다.

## 🔎 더 공부할 것

- semi-sync replication - 커밋 유실과 쓰기 지연의 교환
- fencing / witness 노드 - 2대 구성의 split-brain 보완
- causal reads - 읽기 분리에서 read-your-writes 보장

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#replication` `#failover` `#split-brain` `#read-your-writes` | 2026-11-17 | [예제 보기](../../example/java/replication-lag-and-failover-basics) |
