---
title: groupingBy로 카테고리별 집계
category: java
tags: [stream, collector]
date: 2026-09-30
example: example/java/stream-grouping
status: auto
---
`Collectors.groupingBy`에 downstream collector를 주면 그룹별 집계를 한 번에 할 수 있다.
헷갈린 점: 결과 Map은 순서가 보장되지 않아 `TreeMap::new`로 정렬 Map을 지정했다.
