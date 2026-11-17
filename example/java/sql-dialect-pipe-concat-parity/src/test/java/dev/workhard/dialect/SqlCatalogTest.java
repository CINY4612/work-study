package dev.workhard.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SqlCatalogTest {

    @Test
    void bothDialectsDefineSameStatementIds() {
        // 한쪽만 추가/삭제하면 이 테스트가 실패한다
        assertEquals(SqlCatalog.ORACLE.keySet(), SqlCatalog.MARIADB.keySet());
    }

    @Test
    void mariadbStatementsDoNotUsePipeConcat() {
        SqlCatalog.MARIADB.values().forEach(sql -> assertFalse(SqlCatalog.usesPipeConcat(sql), sql));
    }

    @Test
    void detectorFindsPipeConcatInOracleSql() {
        assertTrue(SqlCatalog.usesPipeConcat(SqlCatalog.ORACLE.get("findOtherRooms")));
        // 문자열 리터럴 안의 || 는 연산자가 아니므로 무시
        assertFalse(SqlCatalog.usesPipeConcat("SELECT 'a||b' FROM dual"));
    }
}
