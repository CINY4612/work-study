<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [design, fallback, configuration]
date: 2026-11-19
example: example/java/inherit-base-spec-not-per-item-values
status: auto
-->

# 기본 설정은 물려받고 개별 값은 따로 두기

> [!NOTE]
> 같은 종류의 새 항목은 01번 설정을 물려받게 하면 설정을 매번 적지 않아도 된다.

> [!WARNING]
> **헷갈린 점** · 단, 항목마다 달라지는 값(좌표 등)까지 물려받으면 조용히 틀린 값이 쓰이니 상속에서 빼야 한다.

## 📖 자세히

### 왜 이렇게 나눌까
같은 종류의 항목(예: BOOK01, BOOK02)은 모양 같은 **종류 공통 값**은 같고, 이미지 좌표 같은 **항목 고유 값**은 다르다. 설정 파일에 항목마다 전부 적으면 누락으로 실패하기 쉽고, 반대로 전부 물려받으면 고유 값이 엉뚱하게 재사용된다.

그래서 해석 순서를 이렇게 둔다.
1. 자기 이름의 설정이 있으면 그것을 그대로 쓴다.
2. 없으면 같은 종류의 01번 설정에서 **공통 값만** 복사한다.
3. 고유 값은 비워 두어 필요한 곳에서 "없음"을 드러나게 한다.
4. 기준이 되는 01번도 없으면 예외로 즉시 알린다.

```java
Spec resolve(String id) {
    Spec own = specs.get(id);
    if (own != null) return own;               // 자기 설정이 우선
    String baseId = id.substring(0, id.length() - 2) + "01";
    Spec base = specs.get(baseId);
    if (base == null) throw new IllegalArgumentException("no base spec: " + id);
    return new Spec(base.shape(), null);       // shape만 상속, sheet는 비움
}
```

### 주의점
- 고유 값을 null로 두면 나중에 NPE가 날 수 있다. 쓰는 쪽에서 "없으면 오류"로 처리하거나 Optional로 감싸는 편이 낫다.
- 상속 규칙이 늘면 어디서 온 값인지 추적하기 어려우니, 공통/고유를 나누는 기준을 문서에 적어 둔다.

## 🔎 더 공부할 것

- Optional - null 대신 값이 없음을 타입으로 표현
- 불변 record와 withX 복사 - 일부만 바꿔 새 객체 만들기
- fail-fast - 기준 설정이 없을 때 조기에 실패시키는 이유

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#design` `#fallback` `#configuration` | 2026-11-19 | [예제 보기](../../example/java/inherit-base-spec-not-per-item-values) |
