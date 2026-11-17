<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [default-value, fail-fast, debugging, lookup]
date: 2026-10-20
example: example/java/empty-lookup-default-masks-miss
status: auto
-->

# 빈 조회 결과를 기본값으로 바꿀 때 생기는 오진

> [!NOTE]
> 조회 결과가 비었을 때 기본값으로 대체하면 '키를 잘못 넘긴 것'과 '정말 데이터가 없는 것'이 구분되지 않아 원인 추적이 어려워진다.

> [!WARNING]
> **헷갈린 점** · 같은 값이 호출 경로마다 다른 식별자(시퀀스 번호 vs 로그인 ID)로 넘어가면 일부 경로에서만 조용히 기본값이 적용된다.

## 📖 자세히

### 한 줄 정의와 왜 필요한지
'조회 실패'와 '조회는 됐는데 결과가 비어 있음'은 다른 상황이다. 이 둘을 모두 `빈 목록 → 기본값`으로 처리하면 호출자가 잘못된 키를 넘겨도 오류 없이 기본 동작을 하므로, 결과가 '그럴듯하게' 틀린다. 증상은 예외가 아니라 "엉뚱한 쪽으로 배정됨" 같은 모호한 형태로 나타난다.

### 동작 원리
1. 계정에는 식별자가 둘 있다. 예: 내부 시퀀스 번호(`id`)와 사용자가 입력하는 로그인 ID(`loginId`). 코드의 변수 이름과 DB 컬럼 이름이 서로 어긋나기 쉽다.
2. 서비스 목록 조회는 한쪽 식별자만 키로 받는다.
3. 다른 식별자를 넘기면 일치하는 행이 없어 빈 목록이 돌아온다.
4. 호출자가 `비었으면 기본 서비스` 규칙을 적용하면 정상 응답처럼 보인다.

```text
호출 경로 A: id 전달       -> 조회 성공 [1, 2]
호출 경로 B: loginId 전달  -> 행 없음 [] -> 기본값 [1]  (오류 없이 조용히 틀림)
```

### 구성 요소/용어 정리
| 용어 | 의미 |
| --- | --- |
| 조회 미스(miss) | 해당 키의 행 자체가 없음. 키가 틀렸을 가능성이 큼 |
| 빈 결과(empty) | 행은 있으나 값이 비어 있음. 정상 데이터일 수 있음 |
| 기본값 대체 | 빈 결과일 때만 쓰는 정책이어야 함 |
| fail-fast | 잘못된 입력은 즉시 예외로 드러내는 방식 |

### 코드로 보기
```java
// 나쁜 예: 미스와 빈 결과를 구분하지 못한다
public List<String> resolveNaive(String key) {
    List<String> s = servicesByAccountId.getOrDefault(key, List.of()); // 미스도 빈 목록이 됨
    return s.isEmpty() ? List.of(DEFAULT_SERVICE) : s;
}

// 좋은 예: 미스는 예외, 등록된 빈 목록만 기본값
public List<String> resolveStrict(String key) {
    List<String> s = servicesByAccountId.get(key); // 없으면 null
    if (s == null) {
        throw new IllegalArgumentException("unknown account: " + key);
    }
    return s.isEmpty() ? List.of(DEFAULT_SERVICE) : s;
}
```

### 비교
| 방식 | 키가 틀렸을 때 | 데이터가 정말 비었을 때 |
| --- | --- | --- |
| naive | 기본값 (조용히 틀림) | 기본값 |
| strict | 예외 (바로 발견) | 기본값 |

### 자주 하는 실수와 주의점
- 기본값을 쓰는 순간에 로그를 남기지 않으면, 나중에 '왜 기본 쪽으로 갔는지' 알 수 없다. 최소한 입력 키와 적용한 기본값을 같이 남긴다.
- 같은 개념(사번, 로그인 ID)에 이름을 하나만 쓰고, 변환은 경계에서 한 번만 한다.
- 쿼리만 고치면 끝나지 않는다. 그 함수를 부르는 모든 경로가 어떤 식별자를 넘기는지 표로 정리해 확인한다.

### 세션에서 나온 질문 정리
- Q. 로그를 보니 후보 노드가 전부 나왔는데 왜 정상처럼 동작했나?
  A. 서비스 목록이 빈 값이라 기본 서비스가 적용됐고, 기본 서비스는 모든 그룹이 갖고 있어 후보가 넓어졌다. 오류가 없으니 설정 문제로 오해하기 쉽다.
- Q. 코드의 `userId`와 DB의 `USER_ID`가 같은 의미인가?
  A. 아니다. 이름이 같아도 의미가 다를 수 있으니 변수 이름이 아니라 실제로 읽는 컬럼 기준으로 따진다.

## 🔎 더 공부할 것

- Optional vs null 반환 - 미스와 빈 값을 타입으로 구분하기
- fail-fast 설계 - 잘못된 입력을 경계에서 거부하기
- MDC 로깅 - 기본값 적용 같은 분기를 추적 가능하게 남기기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#default-value` `#fail-fast` `#debugging` `#lookup` | 2026-10-20 | [예제 보기](../../example/java/empty-lookup-default-masks-miss) |
