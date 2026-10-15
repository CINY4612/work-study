<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [default-value, null-handling, service-layer, refactoring]
date: 2026-10-15
example: example/java/default-value-in-one-place
status: auto
-->

# 요청값 기본값은 서비스 한 곳에서만 정하기

> [!NOTE]
> 입력이 비었을 때의 기본값은 컨트롤러마다 두지 말고 서비스 메서드 한 곳에서만 적용해야 진입점끼리 동작이 어긋나지 않는다.

> [!WARNING]
> **헷갈린 점** · 컨트롤러가 null이면 '전체 대상'으로 분기하면 기본값이 호출부마다 갈라지고, 한 곳만 고치면 나머지는 옛 동작이 남는다.

## 📖 자세히

### 왜 한 곳에서 정해야 할까

같은 요청값(예: 선반 ID)을 받는 진입점이 여러 개면(조회용 API, 등록용 API 등) 각자 `null`을 처리하기 쉽다. 이때 기본값 규칙이 호출부마다 복사된다.

- 한 진입점은 `null`이면 '전체 창고'를 후보로 쓰고, 다른 진입점은 '기본 선반'을 쓰는 식으로 **조용히 어긋난다.**
- 규칙을 바꿀 때(예: 기본 선반 도입) 모든 호출부를 찾아 고쳐야 하고, 하나라도 놓치면 버그가 된다.
- 기본값을 서비스 메서드 안에서 적용하면 호출부는 `null`이든 아니든 **항상 같은 메서드만 부르면 된다.** 분기 자체가 사라진다.

### 잘못된 예: 호출부마다 분기
```java
// 컨트롤러 A, B가 각각 이런 분기를 복사해 둔다
List<String> candidates = shelfId == null
        ? allWarehouses            // 기본값 규칙이 여기에 흩어져 있음
        : shelfService.candidateWarehouses(shelfId);
```

### 올바른 예: 서비스가 기본값을 책임
```java
public List<String> candidateWarehouses(String shelfId) {
    // null/빈 문자열은 여기서만 기본 선반으로 바꾼다
    String effective = (shelfId == null || shelfId.isBlank())
            ? DEFAULT_SHELF_ID : shelfId;
    return warehousesByShelf.getOrDefault(effective, List.of());
}

// 호출부는 분기 없이 그대로 위임
List<String> candidates = shelfService.candidateWarehouses(shelfId);
```

### 같이 챙길 점
- `null`과 빈 문자열(`""`)은 쿼리 파라미터에서 둘 다 '없음'으로 올 수 있으니 같이 처리한다.
- '값이 없음'(기본값 적용)과 '값은 있는데 등록되지 않음'(빈 결과)은 다른 상황이다. 후자에 기본값을 덮어씌우면 오타가 숨겨질 수 있으니 구분한다.
- 기본값 상수도 한 곳에 두고, 다른 클래스가 필요하면 그 상수를 참조한다.

## 🔎 더 공부할 것

- Optional vs null 처리 - 기본값 적용을 null 분기 없이 표현하는 방법
- @RequestParam(defaultValue) - Spring이 컨트롤러 파라미터 기본값을 지원하지만 규칙이 흩어질 수 있는 지점
- Null Object 패턴 - 값이 없는 경우를 객체로 표현해 분기를 없애는 설계

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#default-value` `#null-handling` `#service-layer` `#refactoring` | 2026-10-15 | [예제 보기](../../example/java/default-value-in-one-place) |
