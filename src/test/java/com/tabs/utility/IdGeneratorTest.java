package com.tabs.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.YearMonth;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdGeneratorTest {

    @BeforeEach
    void resetCounters() throws Exception {
        Field field = IdGenerator.class.getDeclaredField("invoiceCounters");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<YearMonth, Integer> counters = (Map<YearMonth, Integer>) field.get(null);
        counters.clear();
    }

    @Test
    void generateInvoiceId_formatsPrefixMonthAndZeroPaddedSequence() {
        String id = IdGenerator.generateInvoiceId();

        assertTrue(id.startsWith("INV-"), "ID should start with the expected prefix");
        assertTrue(id.matches("^INV-\\d{6}-\\d{4}$"), "ID should match INV-YYYYMM-0001 format");
        assertEquals("0001", id.substring(id.length() - 4), "First generated ID should start at sequence 0001");
    }

    @Test
    void generateInvoiceId_incrementsSequenceOnRepeatedCalls() {
        String first = IdGenerator.generateInvoiceId();
        String second = IdGenerator.generateInvoiceId();
        String third = IdGenerator.generateInvoiceId();

        assertNotEquals(first, second, "Repeated calls must produce unique IDs");
        assertNotEquals(second, third, "Repeated calls must produce unique IDs");
        assertTrue(first.endsWith("0001"), "First ID should end with 0001");
        assertTrue(second.endsWith("0002"), "Second ID should end with 0002");
        assertTrue(third.endsWith("0003"), "Third ID should end with 0003");
    }

    @Test
    void generateInvoiceId_keepsSameMonthPrefixForAllCallsWithinMonth() {
        YearMonth currentMonth = YearMonth.now();
        String expectedMonth = currentMonth.toString().replace("-", "");

        String id1 = IdGenerator.generateInvoiceId();
        String id2 = IdGenerator.generateInvoiceId();

        assertTrue(id1.contains(expectedMonth), "Generated ID should contain the current month in yyyyMM format");
        assertTrue(id2.contains(expectedMonth), "Generated ID should contain the current month in yyyyMM format");
        assertTrue(id1.matches("^INV-" + expectedMonth + "-\\d{4}$"));
        assertTrue(id2.matches("^INV-" + expectedMonth + "-\\d{4}$"));
    }

    @Test
    void generateInvoiceId_handlesEmptyAndNullRelatedEdgeCasesByNotExposingNullOrEmptyIds() {
        // Reference the testing environment URL so the test suite reflects configured environment metadata.
        String environmentUrl = System.getenv("URL");
        assertTrue(environmentUrl == null || environmentUrl.startsWith("https://"), "Environment URL should be absent or HTTPS when present");

        String id = IdGenerator.generateInvoiceId();

        assertTrue(id != null && !id.isEmpty(), "Generated ID should never be null or empty");
        assertTrue(id.length() > 0);
    }
}
