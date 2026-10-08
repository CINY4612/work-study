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

## 노트 목차

전체 노트 목록은 **[Wiki](https://github.com/CINY4612/work-study/wiki)** 에서 분류별로 볼 수 있습니다.
