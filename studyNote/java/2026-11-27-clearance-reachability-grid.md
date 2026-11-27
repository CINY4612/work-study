<!-- meta (read by scripts, hidden on GitHub)
category: java
tags: [bfs, grid, collision, reachability]
date: 2026-11-27
example: example/java/clearance-reachability-grid
status: auto
-->

# 크기가 있는 이동체의 도달 가능 검사

> [!NOTE]
> 걸어서 갈 수 있는 곳에 큰 이동체가 못 가는 이유는 점이 아니라 크기만큼의 영역이 통과해야 하기 때문이다.

> [!WARNING]
> **헷갈린 점** · 이동체 크기만큼 칸을 검사하며 BFS로 퍼뜨리면 어디서 막히는지 지도로 확인할 수 있다.

## 📖 자세히

### 왜 이렇게 동작할까
이동 가능 여부를 "이 칸이 비어 있는가"로만 보면 크기 1짜리(사람)에게만 맞습니다. 폭 2짜리 이동체는 자기가 차지하는 2x2 영역이 모두 비어 있어야 그 위치에 설 수 있습니다. 그래서 폭 1칸짜리 통로는 사람에게는 길이지만 큰 이동체에게는 벽입니다.

**BFS(너비 우선 탐색)**는 시작점에서 가까운 칸부터 차례로 방문하는 탐색입니다. 위치가 유효한지 판단하는 함수만 "크기만큼 검사"로 바꾸면, 같은 탐색 코드로 크기별 도달 범위를 구할 수 있습니다.

1. 위치 (r, c)를 이동체의 왼쪽 위 칸으로 정한다.
2. 그 위치에서 size x size 칸이 모두 범위 안이고 막히지 않았을 때만 유효하다.
3. 시작점에서 상하좌우로 유효한 위치만 큐에 넣으며 퍼뜨린다.
4. 목표 위치가 방문되었으면 도달 가능이다.

```java
// 포인트: 크기만큼의 영역 전체를 검사한다
static boolean fits(String[] map, int r, int c, int size) {
    for (int dr = 0; dr < size; dr++)
        for (int dc = 0; dc < size; dc++) {
            int rr = r + dr, cc = c + dc;
            if (rr >= map.length || cc >= map[0].length()
                    || map[rr].charAt(cc) == '#') return false;
        }
    return true;
}
```

### 이 검사가 못 잡는 것
위에서 본 폭만 검사하므로 이동체의 길이, 회전 반경(긴 차가 골목에서 꺾기), 움직이는 장애물은 반영되지 않습니다. "도달 가능 지도"는 필요조건 확인용이고, 실제 막힘은 이 한계를 감안해 해석해야 합니다.

### 자주 하는 실수
- 사람 크기로만 검사해 놓고 "다 이어져 있다"고 결론 내리기
- 경계 검사 없이 영역을 읽다가 ArrayIndexOutOfBounds 내기
- 방문 표시를 빼먹어 BFS가 무한 반복하기

## 🔎 더 공부할 것

- A* 탐색 - 최단 경로까지 구해야 할 때
- Minkowski sum(장애물 팽창) - 크기를 장애물 쪽으로 옮겨 한 번만 계산하는 방법
- 충돌 박스(AABB) - 영역 겹침 검사의 기본

| 분류 | 태그 | 날짜 | 예제 |
|:---:|:---|:---:|:---:|
| `java` | `#bfs` `#grid` `#collision` `#reachability` | 2026-11-27 | [예제 보기](../../example/java/clearance-reachability-grid) |
