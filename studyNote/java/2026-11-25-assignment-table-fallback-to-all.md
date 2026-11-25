<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [routing, assignment-table, fallback, single-point-of-failure]
date: 2026-11-25
example: example/java/assignment-table-fallback-to-all
status: auto
-->

# 카테고리별 서버 배정표와 미배정 시 전체 분배

> [!NOTE]
> 카테고리를 특정 서버에 묶는 배정표를 두되, 표에 없는 카테고리는 전체 서버를 후보로 삼고 표는 재시작 없이 통째로 교체한다.

> [!WARNING]
> **헷갈린 점** · 배정된 서버가 모두 죽었을 때 전체로 넘기면 배정 의도가 깨진다. 후보 없음으로 두고, 서버 하나만 배정하면 장애 경고를 낸다.

## 📖 자세히

### 왜 이렇게 동작할까
카테고리(상품 분류, 업무 종류 등)가 운영 중에 계속 추가되면 "카테고리 → 서버" 연결을 설정 파일에 고정할 수 없다. 그래서 관리자가 저장하는 **배정표**를 두고 라우터가 읽는다. 이때 정할 것이 세 가지다.

1. **미배정은 전체로**: 새로 만든 카테고리는 표에 없다. 이때 실패시키면 카테고리를 만들자마자 서비스가 안 되므로 전체 서버를 후보로 삼는다.
2. **배정됐으면 그 서버만**: 배정된 서버가 모두 죽었을 때 전체로 넘기면 서버를 나눈 이유(예: 같은 카테고리를 한곳에 모으기)가 깨진다. 후보를 비워 호출자가 실패를 알게 한다.
3. **한 서버만 배정하면 경고**: 그 서버가 죽으면 이 카테고리는 처리할 곳이 없다. 저장할 때 경고로 알려 준다.

표는 불변 맵으로 만들어 `volatile` 참조를 통째로 바꾸면, 읽는 쪽이 중간 상태를 볼 일이 없다.

```java
public void replaceAssignments(Map<String, Set<String>> next) {
    Map<String, Set<String>> copy = new HashMap<>();
    next.forEach((k, v) -> copy.put(k, Set.copyOf(v)));
    assignments = Map.copyOf(copy);          // 참조를 한 번에 교체 (재시작 불필요)
}

public List<String> candidates(String category, Set<String> healthy) {
    Set<String> base = assignments.getOrDefault(category, allNodes); // 미배정 = 전체
    return base.stream().filter(healthy::contains).sorted().toList(); // 배정됐으면 그 안에서만
}
```

## 🔎 더 공부할 것

- 최소 부하 노드 선택 - 후보를 거른 뒤 부하가 가장 적은 노드를 고르기
- 캐시 갱신 전략 - 주기 갱신과 저장 시 즉시 갱신의 차이

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#routing` `#assignment-table` `#fallback` `#single-point-of-failure` | 2026-11-25 | [예제 보기](../../example/java/assignment-table-fallback-to-all) |
