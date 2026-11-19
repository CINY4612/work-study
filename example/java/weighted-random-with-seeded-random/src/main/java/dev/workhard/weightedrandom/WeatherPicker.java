package dev.workhard.weightedrandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeatherPicker {
    private final Random random; // 주입 -> 테스트에서 시드 고정 가능

    public WeatherPicker(Random random) {
        this.random = random;
    }

    private static boolean incompatible(Weather prev, Weather next) {
        return (prev == Weather.HOT && next == Weather.SNOW)
                || (prev == Weather.SNOW && next == Weather.HOT);
    }

    public Weather next(Weather prev) {
        List<Weather> allowed = new ArrayList<>();
        for (Weather w : Weather.values()) {
            if (!incompatible(prev, w)) {
                allowed.add(w); // 어색한 조합은 후보에서 먼저 제외
            }
        }
        int total = allowed.stream().mapToInt(w -> w.weight).sum();
        int r = random.nextInt(total); // 후보 가중치 합 안에서 뽑는다
        for (Weather w : allowed) {
            r -= w.weight;
            if (r < 0) {
                return w;
            }
        }
        throw new IllegalStateException("unreachable");
    }
}
