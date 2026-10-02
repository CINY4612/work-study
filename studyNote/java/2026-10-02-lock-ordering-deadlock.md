<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [deadlock, ReentrantLock, concurrency, lock-ordering]
date: 2026-10-02
example: example/java/lock-ordering-deadlock
status: auto
-->

# 락 획득 순서를 고정해서 데드락을 피하는 법

> [!NOTE]
> 데드락은 락 하나를 쥔 채 다른 락을 기다리는 순환에서 생기고, 모든 스레드가 같은 순서로 잡으면 순환이 만들어질 수 없다.

> [!WARNING]
> **헷갈린 점** · DB 행 잠금도 같은 원리라서, 여러 행을 갱신할 때 id 오름차순 같은 고정 순서가 데드락 예방의 기본이다.

## 📖 자세히

### 왜 데드락이 생길까
데드락(교착 상태)은 아래 두 조건이 겹칠 때 생긴다.
1. 실행 주체가 여럿이고 서로 끼어들며 진행한다.
2. 락 하나를 쥔 채로 다른 락을 기다린다.

계좌 이체에서 A→B와 B→A가 동시에 오면, 한쪽은 A를 잡고 B를 기다리고 다른 쪽은 B를 잡고 A를 기다린다. 서로 풀어줄 수 없어 영원히 멈춘다. DB의 행 잠금도 같은 구조라 트랜잭션 둘이 반대 순서로 행을 갱신하면 데드락이 난다. DB는 이를 감지해 한쪽을 강제 롤백하지만, 자바의 `ReentrantLock`은 감지해 주지 않고 그냥 멈춘다.

### 해결: 순서를 고정한다
방향과 상관없이 항상 id가 작은 쪽을 먼저 잡으면 대기 관계가 한 방향으로만 생겨 순환이 불가능하다.

```java
// 잘못된 예: from -> to 순서로 잡으면 반대 방향 이체와 서로 기다림
// from.lock.lock(); to.lock.lock();

// 올바른 예: id가 작은 계좌부터 잡는다
Account first = from.id() < to.id() ? from : to;
Account second = first == from ? to : from;
first.lock().lock();
try {
    second.lock().lock();
    try { /* 출금, 입금 */ }
    finally { second.lock().unlock(); }
} finally { first.lock().unlock(); }
```

순서를 고정할 수 없다면 `tryLock(timeout)`으로 일정 시간 못 잡을 때 포기하고 재시도하는 방법을 쓴다.

## 🔎 더 공부할 것

- ReentrantLock.tryLock(timeout) - 순서를 못 정할 때 대기를 끊는 방법
- synchronized vs ReentrantLock - 공정성과 타임아웃 지원 차이
- DB 데드락과 락 대기 타임아웃 - 감지 후 롤백되는 방식과 재시도 설계
- https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html - 공식 API 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#deadlock` `#ReentrantLock` `#concurrency` `#lock-ordering` | 2026-10-02 | [▶ 실행해보기](../../example/java/lock-ordering-deadlock) |
