<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [migration, versioning, backward-compatibility]
date: 2026-11-19
example: example/java/save-version-migration
status: auto
-->

# 저장 데이터 버전으로 구버전 세이브 이어받기

> [!NOTE]
> 저장 데이터에 version을 넣고, 불러올 때 구버전을 한 단계씩 최신 형식으로 올리면 규칙이 바뀌어도 기존 데이터가 깨지지 않는다.

> [!WARNING]
> **헷갈린 점** · 용량 제한이 줄었다면 넘치는 수량은 버리지 말고 다른 곳으로 옮겨 담는다. 데이터 손실이 없어야 한다.

## 📖 자세히

### 왜 버전이 필요할까
저장 파일은 코드보다 오래 남는다. 서랍 한 칸의 용량을 24개에서 20개로 줄이면, 예전에 저장된 24개짜리 데이터는 새 규칙에서 "있을 수 없는 값"이 된다. 이때 값을 그냥 자르면 4개가 사라지고, 그대로 두면 이후 계산이 틀어진다.

그래서 저장할 때 `version`을 같이 적고, 불러올 때 `version`이 낮으면 **마이그레이션**(옛 형식을 새 형식으로 바꾸는 변환)을 거친다.

### 동작 원리
1. 불러온 데이터의 version을 확인한다.
2. 현재 버전보다 높으면(새 프로그램이 만든 파일) 해석할 수 없으므로 예외로 거부한다.
3. 현재 버전보다 낮으면 v1→v2, v2→v3처럼 **한 단계씩** 변환 함수를 순서대로 적용한다.
4. 단계를 나눠 두면 v1 데이터도 v1→v2→v3 순으로 올라가서 변환 함수를 하나씩만 추가하면 된다.

```java
static SaveData migrate(SaveData d) {
    if (d.version() > CURRENT) throw new IllegalArgumentException("newer save");
    SaveData cur = d;
    while (cur.version() < CURRENT) cur = step(cur); // 한 단계씩 올린다
    return cur;
}

private static SaveData v1ToV2(SaveData d) {
    int overflow = Math.max(0, d.shelfCount() - CAPACITY);
    // 넘친 수량은 버리지 않고 상자로 옮긴다
    return new SaveData(2, d.shelfCount() - overflow, d.boxCount() + overflow);
}
```

### 주의점
- version 필드를 처음부터 넣어 두지 않으면 나중에 "구버전인지" 구분할 방법이 없다.
- 변환은 이미 최신인 데이터에 다시 적용해도 값이 변하지 않아야(멱등) 안전하다.
- 변환 단계를 건너뛰는 코드(v1→v3 직행)를 만들면 중간 규칙이 빠지기 쉽다.

## 🔎 더 공부할 것

- Flyway/Liquibase - DB 스키마에도 같은 버전 마이그레이션 개념이 쓰인다
- Jackson의 @JsonAlias, 기본값 처리 - JSON 필드 추가/변경에 대응하는 방법
- 멱등성(idempotency) - 같은 연산을 여러 번 해도 결과가 같아야 하는 이유

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#migration` `#versioning` `#backward-compatibility` | 2026-11-19 | [예제 보기](../../example/java/save-version-migration) |
