package dev.workhard.weightedrandom;

public enum Weather {
    SUNNY(46), HOT(18), RAIN(17), COLD(16), SNOW(4);

    final int weight; // 클수록 자주 뽑힌다

    Weather(int weight) {
        this.weight = weight;
    }
}
