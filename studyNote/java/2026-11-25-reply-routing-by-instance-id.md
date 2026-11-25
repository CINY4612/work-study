<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [messaging, instance-id, reply-routing, config]
date: 2026-11-25
example: example/java/reply-routing-by-instance-id
status: auto
-->

# 인스턴스 ID가 겹치면 응답이 섞인다

> [!NOTE]
> 응답을 요청한 인스턴스 ID로 되돌려 보내는 구조에서는, 두 인스턴스가 같은 ID를 쓰면 한쪽 응답을 둘 다 받아 섞인다.

> [!WARNING]
> **헷갈린 점** · 서버를 복제할 때 설정 파일을 그대로 복사하면 ID도 같이 복사되므로, 등록 시점에 중복을 거절하게 해 두면 안전하다.

## 📖 자세히

### 왜 이렇게 동작할까
공유 채널(메시지 큐, 소켓 등) 하나로 여러 인스턴스가 요청을 보내는 구조에서는, 응답에 "이 응답은 ID가 X인 인스턴스 것"이라는 표시를 붙여서 구분한다. 받는 쪽은 자기 ID와 같은 응답만 처리한다. 즉 **ID가 곧 주소**다. 같은 ID가 둘이면 주소가 같은 집이 둘인 셈이라, 응답이 양쪽에 모두 배달된다.

서버를 한 대 더 만들 때 설정을 복사하면 ID가 같은 채로 뜨기 쉽다. 에러가 나지 않고 엉뚱한 응답을 처리하기 때문에 원인을 찾기 어렵다. 그래서 등록 시점에 중복이면 바로 실패시키는 것이 좋다.

```java
// 등록 시점에 같은 ID가 이미 있으면 기동 단계에서 실패시킨다
public void register(String id, Consumer<String> handler) {
    if (rejectDuplicate && subs.stream().anyMatch(s -> s.id().equals(id))) {
        throw new IllegalStateException("duplicate instance id: " + id);
    }
    subs.add(new Sub(id, handler));
}

// 응답은 ID가 같은 모든 구독자에게 전달된다 -> ID가 겹치면 둘 다 받는다
public int reply(String targetId, String msg) {
    int n = 0;
    for (Sub s : subs) {
        if (s.id().equals(targetId)) { s.handler().accept(msg); n++; }
    }
    return n;
}
```

## 🔎 더 공부할 것

- Correlation ID - 인스턴스 ID 외에 요청별 ID로 응답을 짝짓는 방법
- Fail-fast 기동 검증 - 잘못된 설정을 기동 단계에서 막기

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#messaging` `#instance-id` `#reply-routing` `#config` | 2026-11-25 | [예제 보기](../../example/java/reply-routing-by-instance-id) |
