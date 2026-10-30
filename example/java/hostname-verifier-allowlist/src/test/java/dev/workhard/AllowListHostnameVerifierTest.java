package dev.workhard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class AllowListHostnameVerifierTest {
    private final AllowListHostnameVerifier verifier =
            new AllowListHostnameVerifier(Set.of("192.0.2.10"));

    @Test
    void allowsListedHost() {
        assertTrue(verifier.verify("192.0.2.10", null));
    }

    @Test
    void rejectsOtherHost() {
        assertFalse(verifier.verify("evil.example.com", null));
    }
}
