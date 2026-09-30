<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [stream, collector]
date: 2026-09-30
example: example/java/stream-grouping
status: auto
-->

# groupingBy로 카테고리별 집계

> [!NOTE]
> `Collectors.groupingBy`에 downstream collector를 주면 그룹별 집계를 한 번에 할 수 있다.

> [!WARNING]
> **헷갈린 점** · 결과 Map은 순서가 보장되지 않아 `TreeMap::new`로 정렬 Map을 지정했다.

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#stream` `#collector` | 2026-09-30 | [▶ 실행해보기](../../example/java/stream-grouping) |
