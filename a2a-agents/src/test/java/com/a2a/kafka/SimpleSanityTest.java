package com.a2a.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleSanityTest {

    @Test
    @DisplayName("Sanity: always passes")
    void alwaysPasses() {
        assertTrue(true);
    }
}
