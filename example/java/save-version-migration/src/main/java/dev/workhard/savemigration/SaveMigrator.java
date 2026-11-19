package dev.workhard.savemigration;

public final class SaveMigrator {
    public static final int CURRENT = 2;
    public static final int CAPACITY = 20; // v2부터 한 칸 용량이 20개

    private SaveMigrator() {
    }

    public static SaveData migrate(SaveData d) {
        // 새 프로그램이 만든 파일은 해석할 수 없으니 거부한다
        if (d.version() > CURRENT) {
            throw new IllegalArgumentException("unsupported newer version: " + d.version());
        }
        SaveData cur = d;
        while (cur.version() < CURRENT) {
            cur = step(cur); // 한 단계씩 올린다
        }
        return cur;
    }

    private static SaveData step(SaveData d) {
        return switch (d.version()) {
            case 1 -> v1ToV2(d);
            default -> throw new IllegalStateException("no migration for v" + d.version());
        };
    }

    private static SaveData v1ToV2(SaveData d) {
        int overflow = Math.max(0, d.shelfCount() - CAPACITY);
        // 넘친 수량은 버리지 않고 상자로 옮겨 담는다
        return new SaveData(2, d.shelfCount() - overflow, d.boxCount() + overflow);
    }
}
