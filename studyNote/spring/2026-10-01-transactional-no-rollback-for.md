<!-- meta (read by scripts, hidden on GitHub)
category: spring
tags: [transactional, rollback, noRollbackFor, spring-tx]
date: 2026-10-01
example: example/spring-boot/transactional-no-rollback
status: auto
-->

# 예외를 던져도 정리 작업은 커밋하고 싶을 때 noRollbackFor를 쓰는 법

> [!NOTE]
> @Transactional 메서드가 RuntimeException을 던지면 그 안에서 한 DB 변경이 전부 롤백되고, noRollbackFor로 예외별로 이 동작을 끌 수 있다.

> [!WARNING]
> **헷갈린 점** · 정리(delete)를 하고 예외로 실패를 알리는 구조면 정리가 사라진다. 로그에 삭제 쿼리가 보여도 커밋되지 않을 수 있다.

## 📖 자세히

### 왜 이렇게 동작할까
스프링의 @Transactional은 프록시(메서드 호출을 가로채는 대리 객체)가 메서드 앞뒤에서 트랜잭션을 열고 닫는 방식이다. 메서드에서 예외가 프록시까지 올라오면 프록시가 롤백할지 커밋할지 정한다.

1. 기본 규칙: `RuntimeException`과 `Error`는 롤백, 체크 예외(`Exception`)는 커밋.
2. 그래서 "장애 노드의 배정 정보를 지우고, 호출자에게는 예외로 실패를 알린다" 같은 흐름에서 예외가 RuntimeException이면 지운 내용까지 롤백된다.
3. `noRollbackFor`에 예외 타입을 적으면 그 예외가 나도 커밋한다.

### 잘못된 예 vs 올바른 예
```java
@Service
public class AssignmentService {
    private final JdbcTemplate jdbc;

    // 잘못된 예: delete 후 RuntimeException -> 프록시가 롤백, 행이 그대로 남는다
    @Transactional
    public void cleanupAndFail(long nodeId) {
        jdbc.update("delete from assignment where node_id = ?", nodeId);
        throw new IllegalStateException("node down");
    }

    // 올바른 예: 이 예외는 롤백 대상에서 제외 -> delete가 커밋된다
    @Transactional(noRollbackFor = IllegalStateException.class)
    public void cleanupAndFailKeep(long nodeId) {
        jdbc.update("delete from assignment where node_id = ?", nodeId);
        throw new IllegalStateException("node down");
    }
}
```

### 주의할 점
- 같은 클래스 안에서 `this.method()`로 부르면 프록시를 거치지 않아 @Transactional 자체가 적용되지 않는다. 다른 빈에서 호출해야 한다.
- 더 근본적인 대안은 정리 작업과 예외를 던지는 로직을 분리하는 것이다. 정리는 `REQUIRES_NEW` 트랜잭션으로 따로 커밋하는 방법도 있다.
- 테스트에서 테스트 메서드에 @Transactional을 붙이면 전체가 한 트랜잭션이 되어 이 차이를 확인할 수 없다. 붙이지 않는다.

## 🔎 더 공부할 것

- Propagation.REQUIRES_NEW - 호출한 트랜잭션과 별개로 커밋해야 할 때 쓴다
- 프록시 기반 AOP의 self-invocation - 같은 클래스 내부 호출에서 @Transactional이 무시되는 이유
- rollbackFor - 체크 예외도 롤백하고 싶을 때의 반대 설정

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `spring` | `#transactional` `#rollback` `#noRollbackFor` `#spring-tx` | 2026-10-01 | [▶ 실행해보기](../../example/spring-boot/transactional-no-rollback) |
