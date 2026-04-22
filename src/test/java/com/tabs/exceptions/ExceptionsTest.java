package com.tabs.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExceptionsTest {

    @Test
    void creditBlockedException_preservesMessage() {
        CreditBlockedException ex = new CreditBlockedException("credit blocked");

        assertEquals("credit blocked", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void customerNotFoundException_preservesMessage() {
        CustomerNotFoundException ex = new CustomerNotFoundException("customer missing");

        assertEquals("customer missing", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void invoiceGenerationException_preservesMessageAndCause() {
        IllegalStateException cause = new IllegalStateException("root cause");
        InvoiceGenerationException ex = new InvoiceGenerationException("invoice failed", cause);

        assertEquals("invoice failed", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals("root cause", ex.getCause().getMessage());
    }

    @Test
    void subscriptionNotFoundException_preservesMessage() {
        SubscriptionNotFoundException ex = new SubscriptionNotFoundException("subscription missing");

        assertEquals("subscription missing", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void exceptions_canBeUsedWithEnvironmentConfiguredUrlReference() {
        String url = System.getenv("URL");
        assertNotNull(url, "URL environment variable should be available in the test environment");

        CreditBlockedException ex = new CreditBlockedException("blocked while accessing: " + url);
        assertTrue(ex.getMessage().contains(url));
    }
}
