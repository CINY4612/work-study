<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [stream, containsAll, anyMatch, filter, set]
date: 2026-10-19
example: example/java/require-all-capabilities-filter
status: auto
-->

# 필요 조건을 모두 갖춘 후보만 남기기

> [!NOTE]
> 후보를 거를 때 '하나라도 지원'(any)이 아니라 '필요한 것을 전부 지원'(containsAll)이어야 엉뚱한 후보가 섞이지 않는다.

> [!WARNING]
> **헷갈린 점** · 기본값처럼 모두가 가진 항목은 필요 조건에서 먼저 빼야 판단이 흔들리지 않는다. 필요 조건이 비면 기본값으로 대체한다.

## 📖 자세히

### 왜 any로 거르면 틀릴까
주문이 {STANDARD, COLD} 두 가지 취급 유형을 요구한다고 하자. 창고 A는 {STANDARD}만, 창고 B는 {STANDARD, COLD}를 처리한다.
`any`(하나라도 겹치면 통과)로 거르면 A도 STANDARD가 겹쳐서 후보가 된다. 하지만 A는 COLD를 처리하지 못하므로 나중에 배정 단계에서 실패하거나 잘못 배정된다.
핵심은 두 가지다.

1. **필요 조건을 먼저 정한다.** 모든 창고가 기본으로 가진 `STANDARD`는 구분력이 없으므로, 다른 유형이 하나라도 있으면 필요 조건에서 뺀다.
2. **후보는 `containsAll(필요 조건)`으로 거른다.** 집합 포함 관계를 쓰면 '전부 갖췄는가'를 한 줄로 표현할 수 있다.

### 잘못된 예 vs 올바른 예
```java
// 잘못된 예: 하나라도 겹치면 후보 -> STANDARD만 겹쳐도 통과
boolean anyMatch = warehouse.capabilities().stream().anyMatch(order::contains);

// 올바른 예: 기본값을 빼고 남은 필요 조건을 전부 갖춘 곳만 통과
Set<String> required = new HashSet<>(orderCaps);
required.remove(DEFAULT_CAP);                         // 모두가 가진 기본값은 판단에서 제외
if (required.isEmpty()) required = Set.of(DEFAULT_CAP); // 기본만 있으면 기본을 요구
boolean ok = warehouse.capabilities().containsAll(required);
```

### 정해 둘 것
필요 조건이 {COLD, FROZEN}처럼 여러 개일 때 '전부'로 볼지 '하나라도'로 볼지는 업무 규칙이다. 규칙을 코드 한 곳(`requiredCapabilities`)에 모아 두면 바꿀 때 테스트도 그 지점만 고치면 된다. 처리 가능한 창고가 없으면 빈 목록이 나오므로, 호출하는 쪽에서 '후보 없음'을 명시적으로 다뤄야 한다.

## 🔎 더 공부할 것

- Set.containsAll - 부분집합 판정이 어떻게 동작하는지, 빈 집합일 때 결과가 무엇인지
- Stream.anyMatch / allMatch / noneMatch - 빈 스트림에서 결과가 다른 점
- Collections.disjoint - 겹치는 항목이 없는지 확인할 때
- https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Set.html - Set 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#stream` `#containsAll` `#anyMatch` `#filter` `#set` | 2026-10-19 | [예제 보기](../../example/java/require-all-capabilities-filter) |
