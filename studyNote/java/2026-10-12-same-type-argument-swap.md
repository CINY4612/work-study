<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [refactoring, type-safety, record, bug]
date: 2026-10-12
example: example/java/same-type-argument-swap
status: auto
-->

# 같은 타입 인자 순서 바뀜을 record로 막기

> [!NOTE]
> long 같은 같은 타입 인자가 연달아 있으면 순서를 바꿔 호출해도 컴파일되고, 테스트 값이 같으면 버그가 드러나지 않는다.

> [!WARNING]
> **헷갈린 점** · ID마다 record로 감싸면 순서가 틀린 호출이 컴파일 에러가 되어 리뷰나 테스트에 기대지 않아도 된다.

## 📖 자세히

### 왜 이렇게 동작할까
컴파일러는 파라미터 **이름**이 아니라 **타입**만 본다. `register(long subsId, long centerId)`를 `register(centerId, subsId)`로 호출해도 둘 다 `long`이라 통과한다. 더 위험한 건 테스트 데이터다. 설정값이나 픽스처가 둘 다 `1`이면 값이 뒤바뀌어도 결과가 같아서 테스트가 계속 통과한다. 실제 운영에서 서로 다른 값이 들어오는 순간 잘못된 컬럼에 저장된다.

해결은 두 가지다.
1. 테스트에서는 인자마다 **서로 다른 값**(예: 5와 7)을 쓴다.
2. 구조적으로는 ID를 `record`(불변 값 객체를 간단히 선언하는 문법)로 감싸 타입을 분리한다. 순서가 틀리면 컴파일 단계에서 막힌다.

```java
// 나쁜 예: 순서를 바꿔도 컴파일됨
String register(long subsId, long centerId) { ... }
register(centerId, subsId);            // 조용히 뒤바뀜

// 좋은 예: 타입이 달라 순서가 틀리면 컴파일 에러
record CenterId(long value) {}
record SubsId(long value) {}
String register(CenterId c, SubsId s) { ... }
register(new CenterId(5), new SubsId(7));
// register(new SubsId(7), new CenterId(5)); // 컴파일 에러
```

## 🔎 더 공부할 것

- record - 컴팩트 생성자로 값 검증하기
- Parameter Object - 인자가 많을 때 객체로 묶는 리팩터링
- MyBatis 파라미터 맵 - 이름 기반 바인딩은 순서 실수에서 안전한 이유

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#refactoring` `#type-safety` `#record` `#bug` | 2026-10-12 | [예제 보기](../../example/java/same-type-argument-swap) |
