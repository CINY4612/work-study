package dev.workhard.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConnectionLedgerTest {

    @Test
    void ownerDown_releasesOnlyItsShare() {
        ConnectionLedger ledger = new ConnectionLedger();
        ledger.allocate("node-1", "daemon-A");
        ledger.allocate("node-1", "daemon-A");
        ledger.allocate("node-2", "daemon-A");

        ledger.releaseAll("node-1");

        assertEquals(1, ledger.total("daemon-A")); // node-2의 몫만 남는다
    }

    @Test
    void releaseAll_isIdempotent() {
        ConnectionLedger ledger = new ConnectionLedger();
        ledger.allocate("node-1", "daemon-A");

        ledger.releaseAll("node-1");
        ledger.releaseAll("node-1");

        assertEquals(0, ledger.total("daemon-A"));
    }

    @Test
    void releaseAfterNormalRelease_doesNotGoNegative() {
        ConnectionLedger ledger = new ConnectionLedger();
        ledger.allocate("node-1", "daemon-A");

        ledger.release("node-1", "daemon-A");
        ledger.release("node-1", "daemon-A"); // 중복 호출
        ledger.releaseAll("node-1");

        assertEquals(0, ledger.total("daemon-A"));
    }
}
