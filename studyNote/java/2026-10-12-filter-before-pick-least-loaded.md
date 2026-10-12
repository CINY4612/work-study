<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [selection, filter, load-balancing, design]
date: 2026-10-12
example: example/java/filter-before-pick-least-loaded
status: auto
-->

# 최소 부하 노드는 후보를 거른 뒤에 고른다

> [!NOTE]
> 하나만 고르는 로직은 후보 필터를 선정 전에 적용해야, 대상 범위 안에서 최선을 고를 수 있다.

> [!WARNING]
> **헷갈린 점** · 선정 후에 걸러내면 전체 1등이 범위 밖일 때 차선을 못 찾고 실패한다.

## 📖 자세히

### 왜 순서가 중요할까

"가장 부하가 적은 노드 1개"를 반환하는 함수는 결과가 하나뿐이라 탈락한 뒤의 대안 정보가 사라진다. 그래서 `전체에서 1등 선정 -> 범위 확인` 순서면, 1등이 범위 밖일 때 범위 안의 2등, 3등이 멀쩡히 있어도 실패한다.

`범위로 후보 축소 -> 축소된 후보에서 1등 선정` 순서로 하면 항상 범위 안의 최선이 나온다.

### 역할 분리

- 호출부: 어떤 노드가 후보인지 결정한다 (서비스 코드, 지역 등 업무 규칙).
- 선정 함수: 넘겨받은 후보 집합으로 살아 있는 노드를 거른 뒤 최소 부하를 고른다. 업무 규칙은 모른다.
- 후보가 `null`이면 "제한 없음"으로 취급해 전체를 대상으로 한다. 빈 집합은 "대상 없음"이므로 구분해야 한다.

### 잘못된 예 vs 올바른 예

```java
// 잘못: 전체 1등을 먼저 고르고 나중에 범위를 확인
Node best = alive.stream().min(BY_LOAD).orElseThrow();
if (!candidateIds.contains(best.id())) {
    throw new IllegalStateException("범위 밖"); // 범위 안 노드가 있어도 실패
}

// 올바름: 후보로 먼저 거르고 그 안에서 선정
List<Node> pool = alive.stream()
    .filter(n -> candidateIds == null || candidateIds.contains(n.id()))
    .toList();
Node best = pool.stream().min(BY_LOAD).orElseThrow();
```

같은 이유로, 선정 뒤에 "요청한 노드를 우선 선택"하는 로직이 있다면 그것도 필터링된 목록을 순회해야 범위 밖 노드가 선택되지 않는다.

## 🔎 더 공부할 것

- Optional vs 예외 - 후보가 비었을 때 반환 방식 선택
- Comparator.comparingInt - 동점 처리(tie-breaker) 규칙 정하기
- https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html - Stream.filter/min 동작

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#selection` `#filter` `#load-balancing` `#design` | 2026-10-12 | [예제 보기](../../example/java/filter-before-pick-least-loaded) |
