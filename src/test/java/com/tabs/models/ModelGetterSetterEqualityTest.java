package com.tabs.models;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class ModelGetterSetterEqualityTest {

    @Test
    void customerShouldMapFieldsAndBeMutable() {
        Customer customer = new Customer("C001", "Alice", "alice@example.com", "REF-1");

        assertEquals("C001", customer.getCustId());
        assertEquals("Alice", customer.getName());
        assertEquals("alice@example.com", customer.getEmail());
        assertEquals("REF-1", customer.getReferredBy());
        assertFalse(customer.isCreditBlocked(), "Credit block should default to false");
        assertNotNull(customer.getPhoneNumbers(), "Phone list should be initialized");
        assertTrue(customer.getPhoneNumbers().isEmpty(), "Phone list should start empty");

        List<String> phones = new ArrayList<>(Arrays.asList("123", "456"));
        customer.setPhoneNumbers(phones);
        customer.setFamilyId("FAM-9");
        customer.setCreditBlocked(true);
        customer.setCustId("C002");
        customer.setName("Bob");
        customer.setEmail("bob@example.com");
        customer.setReferredBy(null);

        assertSame(phones, customer.getPhoneNumbers(), "Setter should keep assigned list reference");
        assertEquals("FAM-9", customer.getFamilyId());
        assertTrue(customer.isCreditBlocked());
        assertEquals("C002", customer.getCustId());
        assertEquals("Bob", customer.getName());
        assertEquals("bob@example.com", customer.getEmail());
        assertNull(customer.getReferredBy());

        customer.getPhoneNumbers().add("789");
        assertEquals(3, phones.size(), "Returned phone list should be mutable and shared");
    }

    @Test
    void customerToStringShouldIncludeAllKeyFields() {
        Customer customer = new Customer("C100", "Carol", "carol@example.com");
        customer.setPhoneNumbers(Arrays.asList("111", "222"));
        customer.setFamilyId("FAM-1");
        customer.setCreditBlocked(true);

        String text = customer.toString();
        assertTrue(text.contains("custId='C100'"));
        assertTrue(text.contains("name='Carol'"));
        assertTrue(text.contains("phoneNumbers=[111, 222]"));
        assertTrue(text.contains("email='carol@example.com'"));
        assertTrue(text.contains("familyId='FAM-1'"));
        assertTrue(text.contains("isCreditBlocked=true"));
    }

    @Test
    void invoiceShouldStoreNullsZerosAndNegativeAmounts() {
        Invoice invoice = new Invoice();
        LocalDate billingDate = LocalDate.of(2024, 2, 29);

        invoice.setInvoiceId("INV-1");
        invoice.setCustId("C001");
        invoice.setSubscriptionId("SUB-1");
        invoice.setPhoneNumber("+15551234567");
        invoice.setBillingDate(billingDate);
        invoice.setBaseFare(0.0);
        invoice.setOverageFare(-1.25);
        invoice.setRoamingCharges(5.5);
        invoice.setFamilyFairnessSurcharge(null);
        invoice.setDiscount(-10.0);
        invoice.setSubTotal(100.0);
        invoice.setGst(18.0);
        invoice.setGrandTotal(108.0);
        invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);

        assertEquals("INV-1", invoice.getInvoiceId());
        assertEquals("C001", invoice.getCustId());
        assertEquals("SUB-1", invoice.getSubscriptionId());
        assertEquals("+15551234567", invoice.getPhoneNumber());
        assertEquals(billingDate, invoice.getBillingDate());
        assertEquals(0.0, invoice.getBaseFare());
        assertEquals(-1.25, invoice.getOverageFare());
        assertEquals(5.5, invoice.getRoamingCharges());
        assertNull(invoice.getFamilyFairnessSurcharge());
        assertEquals(-10.0, invoice.getDiscount());
        assertEquals(100.0, invoice.getSubTotal());
        assertEquals(18.0, invoice.getGst());
        assertEquals(108.0, invoice.getGrandTotal());
        assertEquals(Invoice.PaymentStatus.PAID, invoice.getPaymentStatus());
    }

    @Test
    void invoiceToStringShouldIncludeFieldsAndEnum() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId("INV-2");
        invoice.setCustId("C002");
        invoice.setSubscriptionId("SUB-2");
        invoice.setBillingDate(LocalDate.of(2023, 12, 31));
        invoice.setGrandTotal(123.45);
        invoice.setPaymentStatus(Invoice.PaymentStatus.PENDING);

        String text = invoice.toString();
        assertTrue(text.contains("invoiceId='INV-2'"));
        assertTrue(text.contains("custId='C002'"));
        assertTrue(text.contains("subscriptionId='SUB-2'"));
        assertTrue(text.contains("billingDate=2023-12-31"));
        assertTrue(text.contains("grandTotal=123.45"));
        assertTrue(text.contains("paymentStatus=PENDING"));
    }

    @Test
    void planShouldDefaultFamilySharedFalseAndStoreValues() {
        Plan plan = new Plan();
        assertFalse(plan.isFamilyShared(), "Family shared should default to false");

        plan.setPlanId("P-1");
        plan.setPlanName("Unlimited");
        plan.setMonthlyRental(49.99);
        plan.setDataAllowanceGB(100.0);
        plan.setVoiceAllowedMins(1000.0);
        plan.setSmsAllowed(0);
        plan.setDataOverageRatePerGB(-2.5);
        plan.setVoiceOverageRatePerMin(0.25);
        plan.setSmsOveragePerSMS(0.1);
        plan.setWeekendFreeMinutes(null);
        plan.setFamilyShared(true);

        assertEquals("P-1", plan.getPlanId());
        assertEquals("Unlimited", plan.getPlanName());
        assertEquals(49.99, plan.getMonthlyRental());
        assertEquals(100.0, plan.getDataAllowanceGB());
        assertEquals(1000.0, plan.getVoiceAllowedMins());
        assertEquals(0, plan.getSmsAllowed());
        assertEquals(-2.5, plan.getDataOverageRatePerGB());
        assertEquals(0.25, plan.getVoiceOverageRatePerMin());
        assertEquals(0.1, plan.getSmsOveragePerSMS());
        assertNull(plan.getWeekendFreeMinutes());
        assertTrue(plan.isFamilyShared());
    }

    @Test
    void planToStringShouldReflectCurrentState() {
        Plan plan = new Plan();
        plan.setPlanId("P-2");
        plan.setPlanName("Starter");
        plan.setSmsAllowed(50);

        String text = plan.toString();
        assertTrue(text.contains("planId='P-2'"));
        assertTrue(text.contains("planName='Starter'"));
        assertTrue(text.contains("smsAllowed=50"));
        assertTrue(text.contains("isFamilyShared=false"));
    }

    @Test
    void subscriptionShouldStoreDefaultsAndBoundaryValues() {
        Subscription subscription = new Subscription();

        assertEquals(0.0, subscription.getDataRolloverBalanceGb(), 0.0);

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 15);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);

        subscription.setCustId("C123");
        subscription.setSubscriptionId("S123");
        subscription.setPhoneNumber(null);
        subscription.setMnpStatus(true);
        subscription.setFamilyId("F123");
        subscription.setSubsStartDate(start);
        subscription.setSubsEndDate(end);
        subscription.setDataRolloverBalanceGb(-3.75);
        subscription.setPlanId("PLAN-123");

        assertEquals("C123", subscription.getCustId());
        assertEquals("S123", subscription.getSubscriptionId());
        assertNull(subscription.getPhoneNumber());
        assertTrue(subscription.isMnpStatus());
        assertEquals("F123", subscription.getFamilyId());
        assertEquals(start, subscription.getSubsStartDate());
        assertEquals(end, subscription.getSubsEndDate());
        assertEquals(-3.75, subscription.getDataRolloverBalanceGb(), 0.0);
        assertEquals("PLAN-123", subscription.getPlanId());
    }

    @Test
    void subscriptionToStringShouldIncludeCoreFields() {
        Subscription subscription = new Subscription();
        subscription.setCustId("C500");
        subscription.setSubscriptionId("S500");
        subscription.setPhoneNumber("999");
        subscription.setMnpStatus(true);
        subscription.setFamilyId("F500");
        subscription.setSubsStartDate(LocalDateTime.of(2024, 6, 1, 8, 0));
        subscription.setSubsEndDate(LocalDateTime.of(2025, 6, 1, 8, 0));
        subscription.setDataRolloverBalanceGb(12.5);

        String text = subscription.toString();
        assertTrue(text.contains("custId='C500'"));
        assertTrue(text.contains("subscriptionId='S500'"));
        assertTrue(text.contains("phoneNumber='999'"));
        assertTrue(text.contains("mnpStatus=true"));
        assertTrue(text.contains("dataRolloverBalanceGb=12.5"));
    }

    @Test
    void usageShouldStoreValuesAndAllowMutation() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 3, 10, 12, 30);
        Usage usage = new Usage("SUB-9", start, 0.0, -5.0, 0, true, false);

        assertEquals("SUB-9", usage.getSubscriptionId());
        assertEquals(start, usage.getUsageStartTime());
        assertEquals(0.0, usage.getDataUsedGB());
        assertEquals(-5.0, usage.getVoiceUsedMins());
        assertEquals(0, usage.getSmsUsed());
        assertTrue(usage.isRoaming());
        assertFalse(usage.isInternational());

        usage.setSubscriptionId(null);
        usage.setUsageStartTime(null);
        usage.setDataUsedGB(2.25);
        usage.setVoiceUsedMins(15.5);
        usage.setSmsUsed(7);
        usage.setRoaming(false);
        usage.setInternational(true);

        assertNull(usage.getSubscriptionId());
        assertNull(usage.getUsageStartTime());
        assertEquals(2.25, usage.getDataUsedGB());
        assertEquals(15.5, usage.getVoiceUsedMins());
        assertEquals(7, usage.getSmsUsed());
        assertFalse(usage.isRoaming());
        assertTrue(usage.isInternational());

        Constructor<Usage> ctor = Usage.class.getConstructor(String.class, LocalDateTime.class, Double.class, Double.class, Integer.class, boolean.class, boolean.class);
        assertNotNull(ctor, "Parameterized constructor should be present and accessible");
    }

    @Test
    void usageToStringShouldIncludeAllFields() {
        Usage usage = new Usage("SUB-10", LocalDateTime.of(2024, 7, 1, 9, 0), 1.5, 2.5, 3, false, true);

        String text = usage.toString();
        assertTrue(text.contains("subscriptionId='SUB-10'"));
        assertTrue(text.contains("usageStartTime=2024-07-01T09:00"));
        assertTrue(text.contains("dataUsedGB=1.5"));
        assertTrue(text.contains("voiceUsedMins=2.5"));
        assertTrue(text.contains("smsUsed=3"));
        assertTrue(text.contains("isRoaming=false"));
        assertTrue(text.contains("isInternational=true"));
    }

    @Test
    void customerAndOtherModelsDoNotOverrideEqualsOrHashCodeByDefault() {
        assertNotEquals(new Customer(), new Customer(), "Customer should use Object equality unless overridden");
        assertNotEquals(new Invoice(), new Invoice(), "Invoice should use Object equality unless overridden");
        assertNotEquals(new Plan(), new Plan(), "Plan should use Object equality unless overridden");
        assertNotEquals(new Subscription(), new Subscription(), "Subscription should use Object equality unless overridden");
        assertNotEquals(new Usage(null, null, null, null, null, false, false), new Usage(null, null, null, null, null, false, false), "Usage should use Object equality unless overridden");
    }
}
