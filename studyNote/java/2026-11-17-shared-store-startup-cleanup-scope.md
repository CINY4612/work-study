<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [redis, startup, shared-state, namespace]
date: 2026-11-17
example: example/java/shared-store-startup-cleanup-scope
status: auto
-->

# 공유 저장소에 둔 기동 시 정리 코드의 범위

> [!NOTE]
> 기동 시 'session:*' 같은 키를 전부 지우는 코드는 저장소를 혼자 쓴다는 가정이라, 공유하면 한 노드 재시작이 다른 노드 데이터를 지운다.

> [!WARNING]
> **헷갈린 점** · 저장소를 노드별로 분리하거나 키에 노드 id를 넣어 자기 키만 지우게 해야 한다.

## 📖 자세히

### 왜 이렇게 동작할까
애플리케이션이 시작할 때 "이전 실행의 찌꺼기"를 지우려고 `deleteByPrefix("session:")` 같은 코드를 둔다. 저장소(캐시, Redis 등)를 이 프로세스 하나만 쓴다면 안전하다. 하지만 여러 노드가 같은 저장소를 쓰면, 노드 B를 재시작하는 순간 노드 A가 쓰던 키까지 함께 사라진다. 코드가 "내 것"과 "남의 것"을 구분하지 않기 때문이다.

```java
// 위험: 접두어에 노드 구분이 없다 -> 공유 저장소에서는 남의 키도 지운다
store.deleteByPrefix("session:");

// 안전: 키에 노드 id를 넣고 자기 접두어만 지운다
store.deleteByPrefix("node:" + nodeId + ":session:");
```

- 대안 1: 저장소를 노드마다 따로 둔다(이 경우 정리 코드는 그대로 안전하다).
- 대안 2: 저장소를 공유하되 키 이름에 소유자를 포함한다.
- 이런 가정은 코드에 드러나지 않으므로, 단일 노드를 다중 노드로 바꿀 때 "기동/종료 시 전역 삭제"를 먼저 검색해 보자.

## 🔎 더 공부할 것

- Redis SCAN과 KEYS - 접두어 삭제를 안전하게 하는 법
- key namespace 설계 - 소유자별 키 규칙
- graceful shutdown 훅 - 종료 시 정리 범위

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#redis` `#startup` `#shared-state` `#namespace` | 2026-11-17 | [예제 보기](../../example/java/shared-store-startup-cleanup-scope) |
