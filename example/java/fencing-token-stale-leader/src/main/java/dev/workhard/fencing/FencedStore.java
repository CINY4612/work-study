package dev.workhard.fencing;

// 지금까지 본 가장 큰 토큰보다 작은 토큰의 쓰기를 거부하는 저장소
public class FencedStore {
    private long maxToken = -1;
    private String value;

    public synchronized boolean write(long token, String newValue) {
        if (token < maxToken) {
            return false; // 옛 리더의 쓰기
        }
        maxToken = token;
        value = newValue;
        return true;
    }

    public synchronized String value() {
        return value;
    }
}
