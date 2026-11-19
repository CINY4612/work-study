package dev.workhard.savemigration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SaveMigratorTest {

    @Test
    void v1_overflow_moves_to_box() {
        SaveData result = SaveMigrator.migrate(new SaveData(1, 24, 3));
        assertEquals(new SaveData(2, 20, 7), result); // 24 -> 20, 넘친 4개는 상자로
    }

    @Test
    void v1_within_capacity_only_bumps_version() {
        SaveData result = SaveMigrator.migrate(new SaveData(1, 10, 3));
        assertEquals(new SaveData(2, 10, 3), result);
    }

    @Test
    void latest_version_is_unchanged_and_migration_is_idempotent() {
        SaveData latest = new SaveData(2, 20, 0);
        assertEquals(latest, SaveMigrator.migrate(latest));
        assertEquals(latest, SaveMigrator.migrate(SaveMigrator.migrate(latest)));
    }

    @Test
    void newer_version_is_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SaveMigrator.migrate(new SaveData(3, 1, 1)));
    }
}
