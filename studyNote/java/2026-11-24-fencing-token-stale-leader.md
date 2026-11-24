<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [split-brain, fencing-token, leader-election, concurrency]
date: 2026-11-24
example: example/java/fencing-token-stale-leader
status: auto
-->

# 펜싱 토큰으로 옛 리더의 쓰기 막기

> [!NOTE]
> 락을 얻을 때마다 커지는 토큰을 발급하고, 저장소가 더 작은 토큰의 쓰기를 거부하면 옛 리더가 뒤늦게 써도 막힌다.

> [!WARNING]
> **헷갈린 점** · 서버 2대에서는 과반 투표가 안 돼 split-brain이 생길 수 있어서, 리더 판정만으로는 부족하고 쓰는 쪽에서 막아야 한다.

## 📖 자세히

### 왜 필요할까
Active 노드를 하나만 두는 구조(Primary-Secondary)에서 네트워크가 끊기면 두 노드가 서로 죽었다고 보고 둘 다 Primary로 동작할 수 있습니다. 이를 split-brain(뇌 분리)이라고 합니다. 노드가 2대면 과반(quorum) 투표도 못 합니다.

더 까다로운 경우는 옛 리더가 멈췄다가(GC, 네트워크 지연) 락이 만료된 줄 모르고 깨어나 쓰는 것입니다. 자기는 아직 리더라고 믿으니 쓰기 직전 검사도 소용없습니다.

### 동작 원리
1. 락을 새로 얻을 때마다 단조 증가하는 토큰(1, 2, 3...)을 받습니다.
2. 쓰기 요청에 토큰을 같이 보냅니다.
3. 저장소는 지금까지 본 가장 큰 토큰을 기억하고, 그보다 작은 토큰의 쓰기는 거부합니다.

```text
리더 A(토큰1) 멈춤 ... 리더 B(토큰2) write OK
A 깨어나 write(토큰1) -> 저장소: 1 < 2, 거부
```

### 코드로 보기
```java
public synchronized boolean write(long token, String v) {
    if (token < maxToken) return false; // 옛 리더의 쓰기는 거부
    maxToken = token;                   // 본 토큰 중 최대값 갱신
    value = v;
    return true;
}
```

### 주의점
- 토큰 검사는 저장소(쓰는 쪽)에서 해야 합니다. 리더 쪽에서 검사하면 멈춘 사이에 바뀐 사실을 모릅니다.
- 토큰은 반드시 단조 증가해야 하므로 DB 시퀀스나 락 서비스의 버전 번호를 씁니다.

## 🔎 더 공부할 것

- quorum과 과반 투표 - 2대 구성이 왜 어려운지
- lease(임대) 기반 리더 선출 - 만료 시각으로 리더를 정하는 방식
- 낙관적 락(@Version) - 같은 원리를 DB 행에 적용

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#split-brain` `#fencing-token` `#leader-election` `#concurrency` | 2026-11-24 | [예제 보기](../../example/java/fencing-token-stale-leader) |
