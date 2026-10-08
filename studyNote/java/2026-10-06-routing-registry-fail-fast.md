<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [routing, registry, enum, fail-fast]
date: 2026-10-06
example: example/java/routing-registry-fail-fast
status: auto
-->

# 규칙 레지스트리로 요청 목적지 관리 (주소가 없으면 fail-fast)

> [!NOTE]
> 규칙 키마다 목적지(enum)를 등록하고 목적지별 주소를 따로 조회하면, 새 목적지를 추가해도 라우팅 코드를 거의 안 고친다.

> [!WARNING]
> **헷갈린 점** · 주소가 없을 때 기본 목적지로 보내면 엉뚱한 서버가 요청을 받는다. 숨기지 말고 즉시 오류로 드러내야 한다.

## 📖 자세히

### 왜 이렇게 설계할까
- 목적지가 하나뿐일 때는 `if`로 충분하지만, 둘 이상이 되면 "어떤 키가 어디로 가는가"가 코드 곳곳에 흩어진다. 그래서 **레지스트리**(키 → 목적지 매핑을 한곳에 모아 둔 표)를 두고, 라우터는 표를 조회만 한다.
- 목적지를 `String`이 아니라 `enum`으로 두면 오타가 컴파일 단계에서 걸리고, 목적지 목록이 코드에 명시된다.
- **fail-fast**(문제를 발견한 즉시 실패시키는 방식): 주소 설정이 비어 있을 때 "일단 기본 목적지로" 보내면 요청이 조용히 잘못된 곳에 도착한다. 오류가 나야 설정 누락을 바로 알 수 있다.
- 같은 키를 두 번 등록하면 나중 값이 이전 값을 덮어써 버린다. `putIfAbsent`로 중복 등록을 막으면 이런 실수가 시작 시점에 드러난다.

```java
public void register(String key, Destination d) {
    // 중복 등록은 덮어쓰지 않고 바로 실패
    if (rules.putIfAbsent(key, d) != null) {
        throw new IllegalStateException("duplicate rule: " + key);
    }
}

public String resolve(String key) {
    Destination d = rules.get(key);
    if (d == null) throw new NoRouteException("no rule: " + key);
    String address = addresses.get(d);
    // 주소가 없으면 다른 목적지로 대체하지 않고 실패
    if (address == null) throw new NoRouteException("no address for " + d);
    return address;
}
```

### 테스트 팁
목적지 enum 전체가 주소 설정에 있는지 확인하는 테스트를 하나 두면, enum에 값을 추가하고 설정을 빠뜨렸을 때 바로 실패한다.

## 🔎 더 공부할 것

- EnumMap - enum 키 전용 맵이라 목적지별 설정 보관에 적합하다
- Fail-fast vs graceful degradation - 어떤 상황에 기본값 대체가 맞는지
- Map.putIfAbsent / merge - 중복 키 처리 방식 비교
- Spring @ConfigurationProperties - 목적지별 주소를 설정에서 바인딩하는 법

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#routing` `#registry` `#enum` `#fail-fast` | 2026-10-06 | [예제 보기](../../example/java/routing-registry-fail-fast) |
