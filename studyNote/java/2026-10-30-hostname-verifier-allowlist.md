<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [https, ssl, hostname-verifier, san]
date: 2026-10-30
example: example/java/hostname-verifier-allowlist
status: auto
-->

# HTTPS 인증서 신뢰와 호스트명 검증은 별개

> [!NOTE]
> Java HTTPS는 인증서 신뢰 검사와 호스트명 검증(접속 주소가 인증서 SAN에 있는지)을 따로 한다. 신뢰 검사만 꺼도 IP 접속은 실패한다.

> [!WARNING]
> **헷갈린 점** · 'No subject alternative names matching IP' 오류는 IP 설정 오류가 아니라 인증서에 그 IP가 없다는 뜻이다.

## 📖 자세히

### 왜 이렇게 동작할까
HTTPS 연결은 두 가지를 확인한다.
1. 인증서 신뢰: 믿을 수 있는 CA가 서명했는가 (`TrustManager`)
2. 호스트명 검증: 접속한 주소가 인증서의 SAN(Subject Alternative Name, 인증서가 유효한 도메인/IP 목록)에 있는가 (`HostnameVerifier`)

테스트에서 trust-all `TrustManager`로 1번만 끄면 2번이 남는다. 그래서 `https://192.0.2.10`처럼 IP로 접속하면, 인증서에 도메인만 있을 때 실패한다. 또 `HostnameVerifier`는 만들기만 해서는 소용없고 연결에 `setHostnameVerifier`로 적용해야 한다.

### 해결 선택지
- 인증서 도메인으로 호출하고 테스트 PC의 hosts 파일에 IP를 매핑한다 (코드 수정 없음, 권장).
- 테스트용 인증서를 IP SAN을 넣어 다시 발급한다.
- 꼭 코드로 풀어야 하면 `(h, s) -> true`처럼 전부 허용하지 말고 허용 목록 호스트만 통과시킨다.

```java
HostnameVerifier v = new AllowListHostnameVerifier(Set.of("192.0.2.10"));
connection.setHostnameVerifier(v); // 적용하지 않으면 기본 검증이 동작한다
```

## 🔎 더 공부할 것

- SSLContext와 TrustManager - 인증서 신뢰 검사의 구성
- SAN과 CN - 최신 클라이언트가 SAN만 보는 이유
- https://docs.oracle.com/en/java/javase/21/ - HttpsURLConnection 문서

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#https` `#ssl` `#hostname-verifier` `#san` | 2026-10-30 | [예제 보기](../../example/java/hostname-verifier-allowlist) |
