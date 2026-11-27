package dev.workhard.clearance;

import java.util.ArrayDeque;
import java.util.Deque;

public class Reachability {

    // size x size 영역이 모두 범위 안이고 벽('#')이 아닐 때만 설 수 있다
    static boolean fits(String[] map, int r, int c, int size) {
        if (r < 0 || c < 0) return false;
        for (int dr = 0; dr < size; dr++) {
            for (int dc = 0; dc < size; dc++) {
                int rr = r + dr;
                int cc = c + dc;
                if (rr >= map.length || cc >= map[0].length()) return false;
                if (map[rr].charAt(cc) == '#') return false;
            }
        }
        return true;
    }

    // BFS: 시작 위치에서 유효한 위치만 따라가며 목표에 닿는지 본다
    public static boolean canReach(String[] map, int sr, int sc, int tr, int tc, int size) {
        if (!fits(map, sr, sc, size) || !fits(map, tr, tc, size)) return false;
        boolean[][] seen = new boolean[map.length][map[0].length()];
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {sr, sc});
        seen[sr][sc] = true;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            if (cur[0] == tr && cur[1] == tc) return true;
            for (int[] d : dirs) {
                int nr = cur[0] + d[0];
                int nc = cur[1] + d[1];
                if (nr < 0 || nc < 0 || nr >= map.length || nc >= map[0].length()) continue;
                if (seen[nr][nc] || !fits(map, nr, nc, size)) continue;
                seen[nr][nc] = true;
                queue.add(new int[] {nr, nc});
            }
        }
        return false;
    }
}
