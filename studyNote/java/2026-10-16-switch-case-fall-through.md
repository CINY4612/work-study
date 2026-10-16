<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [switch, fall-through, java21, bug]
date: 2026-10-16
example: example/java/switch-case-fall-through
status: auto
-->

# switch의 break 누락과 fall-through

> [!NOTE]
> 전통적인 `case X:` switch는 break가 없으면 다음 case 본문까지 이어서 실행되고, 화살표(`->`) switch는 이어지지 않는다.

> [!WARNING]
> **헷갈린 점** · 컴파일 오류도 예외도 없어서, 한 case에 값이 하나 더 담기는 식으로 조용히 틀린 응답이 나간다.

## 📖 자세히

### 왜 이렇게 동작할까
전통적인 switch는 `case` 라벨을 '점프할 위치'로만 쓴다. 일치하는 라벨로 이동한 뒤에는 `break`를 만날 때까지 아래 문장을 그대로 실행한다. 이것을 fall-through(아래 case로 떨어져 실행됨)라고 한다. 문법상 문제가 없으니 컴파일러도 막지 않는다.

요청 타입별로 응답에 넣을 값을 정하는 코드에서 자주 생긴다. 1번 case에 break를 빼먹으면 1번 요청도 2번 case의 값을 함께 받는다. 테스트가 정확한 키 집합을 검사하지 않으면 그대로 배포될 수 있다.

### 잘못된 예
```java
switch (type) {
    case "1":
        r.put("push", "...");
        // break 누락: 아래 case "2" 본문도 실행된다
    case "2":
        r.put("email", "...");
        break;
    default:
        throw new IllegalArgumentException(type);
}
```

### 올바른 예 (Java 14+ 화살표 switch)
```java
switch (type) {
    case "1" -> r.put("push", "...");   // 화살표 형식은 fall-through가 없다
    case "2" -> r.put("email", "...");
    default -> throw new IllegalArgumentException(type);
}
```
일부러 이어서 실행해야 한다면 전통 형식에 `// fall through` 주석을 남겨 실수와 구별되게 한다. 응답 키 전체를 `assertEquals`로 비교하면 이런 누락을 테스트가 잡는다.

## 🔎 더 공부할 것

- switch 표현식(yield) - 값을 반환하는 switch와 모든 경우를 강제하는 방식
- sealed 인터페이스 + 패턴 매칭 switch - default 없이 누락을 컴파일러가 잡게 하기
- https://docs.oracle.com/en/java/javase/21/language/switch-expressions-and-statements.html - 공식 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#switch` `#fall-through` `#java21` `#bug` | 2026-10-16 | [예제 보기](../../example/java/switch-case-fall-through) |
