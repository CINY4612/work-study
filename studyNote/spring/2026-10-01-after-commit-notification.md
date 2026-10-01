<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [transaction, TransactionSynchronization, afterCommit, side-effect]
date: 2026-10-01
example: example/spring-boot/after-commit-notification
status: auto
-->

# 트랜잭션이 커밋된 뒤에만 외부 알림을 보내는 법

> [!NOTE]
> 외부 호출(알림, 메시지 발송)은 TransactionSynchronization.afterCommit에 등록하면 커밋이 확정된 뒤에만 실행된다.

> [!WARNING]
> **헷갈린 점** · @Transactional 메서드 안에서 바로 호출하면 이후 롤백돼도 알림은 이미 나가서, DB 상태와 외부 상태가 어긋난다.

## 📖 자세히

### 왜 이렇게 동작할까
트랜잭션 메서드 안에서 외부 시스템을 바로 호출하면 두 가지 문제가 생긴다.

1. 호출한 뒤 예외가 나 롤백돼도 이미 나간 알림은 되돌릴 수 없다. (DB에는 없는 주문을 알림으로는 받게 된다.)
2. 아직 커밋 전이라, 알림을 받은 쪽이 바로 조회하면 데이터가 안 보일 수 있다.

스프링은 현재 트랜잭션에 콜백을 붙이는 `TransactionSynchronizationManager.registerSynchronization`을 제공한다. 등록한 `afterCommit()`은 커밋이 성공한 직후에만 호출되고, 롤백되면 호출되지 않는다.

```java
@Transactional
public void place(String item, boolean fail) {
    jdbc.update("insert into orders(item) values (?)", item);

    // 지금 보내지 않고, '커밋 성공 후' 할 일로 예약만 한다
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notifier.send(item); // 롤백되면 호출되지 않는다
            }
        });

    if (fail) throw new IllegalStateException("rollback");
}
```

### 주의할 점
- 활성 트랜잭션이 없을 때 `registerSynchronization`을 부르면 예외가 난다. 트랜잭션 안에서만 호출해야 한다.
- `afterCommit` 안에서 예외가 나도 이미 커밋된 데이터는 되돌아가지 않는다. 알림 실패가 본 작업을 망치지 않아야 하면 try/catch로 처리하거나 비동기로 넘긴다.
- `afterCommit` 시점에는 트랜잭션 리소스가 아직 묶여 있어서, 여기서 DB 쓰기를 하려면 새 트랜잭션(REQUIRES_NEW)이 필요하다.
- 같은 목적의 선언적 방식으로 `@TransactionalEventListener(phase = AFTER_COMMIT)`도 있다.

## 🔎 더 공부할 것

- @TransactionalEventListener - 이벤트 발행 방식으로 커밋 후 처리를 선언적으로 쓰는 법
- Transactional Outbox 패턴 - 알림 유실까지 막고 싶을 때의 설계
- @Async - 커밋 후 알림을 별도 스레드로 보내 응답 지연을 피하는 법
- https://docs.spring.io/spring-framework/reference/data-access/transaction.html - 스프링 트랜잭션 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#transaction` `#TransactionSynchronization` `#afterCommit` `#side-effect` | 2026-10-01 | [▶ 실행해보기](../../example/spring-boot/after-commit-notification) |
