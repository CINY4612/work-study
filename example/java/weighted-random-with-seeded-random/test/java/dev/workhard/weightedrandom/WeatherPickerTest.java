package dev.workhard.weightedrandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class WeatherPickerTest {

    @Test
    void sameSeedGivesSameSequence() {
        WeatherPicker a = new WeatherPicker(new Random(42));
        WeatherPicker b = new WeatherPicker(new Random(42));
        Weather pa = Weather.SUNNY;
        Weather pb = Weather.SUNNY;
        for (int i = 0; i < 100; i++) {
            pa = a.next(pa);
            pb = b.next(pb);
            assertEquals(pa, pb); // 시드가 같으면 결과도 같다
        }
    }

    @Test
    void neverJumpsBetweenHotAndSnow() {
        WeatherPicker picker = new WeatherPicker(new Random(7));
        Weather prev = Weather.SUNNY;
        for (int i = 0; i < 3000; i++) {
            Weather next = picker.next(prev);
            assertFalse(prev == Weather.HOT && next == Weather.SNOW);
            assertFalse(prev == Weather.SNOW && next == Weather.HOT);
            prev = next;
        }
    }

    @Test
    void heavierWeightAppearsMoreOften() {
        WeatherPicker picker = new WeatherPicker(new Random(1));
        int sunny = 0;
        int snow = 0;
        Weather prev = Weather.SUNNY;
        for (int i = 0; i < 3000; i++) {
            prev = picker.next(prev);
            if (prev == Weather.SUNNY) sunny++;
            if (prev == Weather.SNOW) snow++;
        }
        assertTrue(sunny > snow * 3); // 가중치 46 vs 4
    }
}
