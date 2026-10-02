# work-study

일하면서 새롭게 알게 된 내용을 **1~2줄 노트(`studyNote/`)** 와 **직접 실행해 볼 수 있는 예제(`example/`)** 로 정리하는 레포입니다.
회사 코드는 포함하지 않으며, 모든 예제는 범용 도메인(도서, 주문, 할 일)으로 다시 작성했습니다.

## 구조

```
studyNote/<category>/YYYY-MM-DD-slug.md   # 1~2줄 노트 (분류 정보는 숨은 meta 블록)
example/<java|spring-boot|node-express|javascript|typescript>/<project>   # 실행 가능한 예제
```

## 예제 실행

| 종류 | 실행 | 테스트 |
|---|---|---|
| java / spring-boot | `./gradlew run` / `./gradlew bootRun` | `./gradlew test` |
| node-express | `npm install && npm start` | `npm test` |

JDK 21, Node 22 기준이며 외부 DB/서비스가 필요 없습니다 (H2, 인메모리).

## 태그

`study/<category>/<yyyyMMdd-HHmm>` - 카테고리별 학습 이력. 예: `git tag -l "study/spring/*"`

## 노트 인덱스

<!-- INDEX:START -->
Total notes: 10

### java (4)
| Date | Note | Tags | Example |
|---|---|---|---|
| 2026-10-02 | [TTL 대신 만료 시각과 주입한 Clock으로 heartbeat 생존 판정을 하는 법](studyNote/java/2026-10-02-heartbeat-lease-expiry.md) | `#heartbeat` `#lease` `#ttl` `#clock` `#concurrency` | [run](example/java/heartbeat-lease-expiry) |
| 2026-10-02 | [락 획득 순서를 고정해서 데드락을 피하는 법](studyNote/java/2026-10-02-lock-ordering-deadlock.md) | `#deadlock` `#ReentrantLock` `#concurrency` `#lock-ordering` | [run](example/java/lock-ordering-deadlock) |
| 2026-10-01 | [헬스체크에서 직전 상태를 기억해 연속 실패 판정과 상태 변화를 잡는 법](studyNote/java/2026-10-01-health-check-threshold.md) | `#health-check` `#state` `#threshold` `#monitoring` | [run](example/java/health-check-threshold) |
| 2026-09-30 | [groupingBy로 카테고리별 집계](studyNote/java/2026-09-30-stream-grouping.md) | `#stream` `#collector` | [run](example/java/stream-grouping) |

### javascript (1)
| Date | Note | Tags | Example |
|---|---|---|---|
| 2026-09-30 | [수집은 무료로, 비싼 호출은 배치 1회로 분리하는 법](studyNote/javascript/2026-09-30-batch-candidates.md) | `#automation` `#jsonl` `#batch` | [run](example/javascript/batch-candidates) |

### node-express (2)
| Date | Note | Tags | Example |
|---|---|---|---|
| 2026-10-02 | [서버 세션과 JWT는 강제 로그아웃 가능 여부가 왜 다른지 이해하는 법](studyNote/node-express/2026-10-02-session-vs-jwt-force-logout.md) | `#jwt` `#session` `#auth` `#revocation` | [run](example/node-express/session-vs-jwt-force-logout) |
| 2026-09-30 | [Express 라우트를 앱 팩토리로 분리해 테스트](studyNote/node-express/2026-09-30-app-factory-test.md) | `#express` `#test` | [run](example/node-express/health-route) |

### spring (3)
| Date | Note | Tags | Example |
|---|---|---|---|
| 2026-10-01 | [예외를 던져도 정리 작업은 커밋하고 싶을 때 noRollbackFor를 쓰는 법](studyNote/spring/2026-10-01-transactional-no-rollback-for.md) | `#transactional` `#rollback` `#noRollbackFor` `#spring-tx` | [run](example/spring-boot/transactional-no-rollback) |
| 2026-10-01 | [트랜잭션이 커밋된 뒤에만 외부 알림을 보내는 법](studyNote/spring/2026-10-01-after-commit-notification.md) | `#transaction` `#TransactionSynchronization` `#afterCommit` `#side-effect` | [run](example/spring-boot/after-commit-notification) |
| 2026-09-30 | [@Valid로 요청 본문 검증](studyNote/spring/2026-09-30-valid-request-body.md) | `#validation` `#rest` | [run](example/spring-boot/rest-validation) |
<!-- INDEX:END -->
