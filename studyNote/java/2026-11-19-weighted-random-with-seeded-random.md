<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [random, weighted-choice, testing, simulation]
date: 2026-11-19
example: example/java/weighted-random-with-seeded-random
status: auto
-->

# 가중치 랜덤 선택과 시드 고정 테스트

> [!NOTE]
> 가중치 합 안에서 난수를 뽑아 누적하며 고르고, Random을 주입하면 시드 고정으로 같은 결과를 재현할 수 있다.

> [!WARNING]
> **헷갈린 점** · '폭염 다음 날 눈' 같은 어색한 조합은 후보에서 먼저 빼고 뽑으면 된다.

## 📖 자세히

### 왜 이렇게 동작할까
가중치 랜덤은 `[0, 가중치 합)` 범위에서 난수 하나를 뽑고, 항목을 순서대로 지나며 가중치를 빼 가다가 0 미만이 되는 항목을 고른다. 가중치가 클수록 그 구간이 넓어 자주 뽑힌다.

`new Random()`을 코드 안에서 만들면 테스트가 매번 달라진다. `Random`을 **주입**하면 `new Random(42)`처럼 시드(난수 시작값)를 고정해 같은 순서의 결과를 재현할 수 있다.

```java
Weather next(Weather prev) {
    List<Weather> allowed = new ArrayList<>();
    for (Weather w : Weather.values()) {
        if (!incompatible(prev, w)) allowed.add(w); // 먼저 후보를 거른다
    }
    int total = allowed.stream().mapToInt(w -> w.weight).sum();
    int r = random.nextInt(total);                   // 거른 후보의 합 안에서 뽑는다
    for (Weather w : allowed) {
        r -= w.weight;
        if (r < 0) return w;
    }
    throw new IllegalStateException();
}
```

### 주의점
- 불가능 조합은 뽑은 뒤 다시 뽑기(재시도)보다 **후보에서 먼저 제외**하는 편이 분포가 덜 왜곡되고 무한 반복 위험도 없다.
- 확률 테스트는 정확한 값 대신 '수천 번 돌려도 규칙 위반이 0번', '대략 비율 범위 안'처럼 성질을 검사한다.

## 🔎 더 공부할 것

- Random vs ThreadLocalRandom vs SecureRandom - 용도에 따른 선택
- 마르코프 체인 - 전날 상태에 따라 다음 확률을 바꾸는 모델
- 속성 기반 테스트 - 많은 입력으로 규칙 위반 찾기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#random` `#weighted-choice` `#testing` `#simulation` | 2026-11-19 | [예제 보기](../../example/java/weighted-random-with-seeded-random) |
