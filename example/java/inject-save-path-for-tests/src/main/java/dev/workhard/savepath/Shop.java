package dev.workhard.savepath;

import java.io.IOException;

/** 영업을 시작하면 부수 효과로 자동 저장하는 가게. */
public class Shop {
    private final SaveStore store;
    private final boolean autoSave;
    private int stock;

    public Shop(SaveStore store, boolean autoSave) {
        this.store = store;
        this.autoSave = autoSave;
    }

    public void openForBusiness() throws IOException {
        stock = 100; // 테스트용으로도 상태가 바뀐다
        if (autoSave) {
            store.write("stock=" + stock); // 숨은 부수 효과
        }
    }
}
