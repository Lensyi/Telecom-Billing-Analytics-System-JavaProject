package com.tabs.utility;

import com.tabs.models.Plan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanConfigTest {

    @Test
    void getPlanById_returnsSystemPlanForKnownId() {
        Plan plan = PlanConfig.getPlanById("UNIV-01");

        assertNotNull(plan);
        assertSame(PlanConfig.SYSTEM_PLAN, plan);
        assertEquals("UNIV-01", plan.getPlanId());
        assertEquals("Universal Plan 799", plan.getPlanName());
        assertEquals(799.0, plan.getMonthlyRental());
        assertEquals(100.0, plan.getVoiceAllowedMins());
        assertEquals(5000.0, plan.getDataAllowanceGB());
        assertEquals(100, plan.getSmsAllowed());
        assertEquals(9.0, plan.getVoiceOverageRatePerMin());
        assertEquals(0.9, plan.getDataOverageRatePerGB());
        assertEquals(0.5, plan.getSmsOveragePerSMS());
        assertEquals(500.0, plan.getWeekendFreeMinutes());
        assertTrue(plan.isFamilyShared());
    }

    @Test
    void getPlanById_returnsLitePlanForKnownId() {
        Plan plan = PlanConfig.getPlanById("LITE-01");

        assertNotNull(plan);
        assertSame(PlanConfig.SYSTEM_PLAN_LITE, plan);
        assertEquals("LITE-01", plan.getPlanId());
        assertEquals("Lite Plan 599", plan.getPlanName());
        assertEquals(599.0, plan.getMonthlyRental());
        assertEquals(70.0, plan.getVoiceAllowedMins());
        assertEquals(3000.0, plan.getDataAllowanceGB());
        assertEquals(50, plan.getSmsAllowed());
        assertEquals(9.0, plan.getVoiceOverageRatePerMin());
        assertEquals(0.9, plan.getDataOverageRatePerGB());
        assertEquals(0.5, plan.getSmsOveragePerSMS());
        assertEquals(500.0, plan.getWeekendFreeMinutes());
        assertTrue(plan.isFamilyShared());
    }

    @Test
    void getPlanById_isCaseSensitiveAndRejectsMalformedKeys() {
        assertNull(PlanConfig.getPlanById("univ-01"));
        assertNull(PlanConfig.getPlanById("UNIV-01 "));
        assertNull(PlanConfig.getPlanById(" UNIV-01"));
        assertNull(PlanConfig.getPlanById("UNIV_01"));
        assertNull(PlanConfig.getPlanById(""));
        assertNull(PlanConfig.getPlanById("   "));
        assertNull(PlanConfig.getPlanById("LITE-1"));
        assertNull(PlanConfig.getPlanById("LITE-01-extra"));
    }

    @Test
    void getPlanById_returnsNullForMissingOrNullPlanId() {
        assertNull(PlanConfig.getPlanById("MISSING-PLAN"));
        assertNull(PlanConfig.getPlanById(null));
    }

    @Test
    void systemPlansAreRegisteredAndRemainDistinct() {
        assertNotNull(PlanConfig.SYSTEM_PLAN);
        assertNotNull(PlanConfig.SYSTEM_PLAN_LITE);
        assertNotSame(PlanConfig.SYSTEM_PLAN, PlanConfig.SYSTEM_PLAN_LITE);
        assertEquals("UNIV-01", PlanConfig.SYSTEM_PLAN.getPlanId());
        assertEquals("LITE-01", PlanConfig.SYSTEM_PLAN_LITE.getPlanId());
        assertNotEquals(PlanConfig.SYSTEM_PLAN.getMonthlyRental(), PlanConfig.SYSTEM_PLAN_LITE.getMonthlyRental());
    }

    @Test
    void planDefaultConstructorSetsFamilySharedFalseAndSettersExposeProperties() {
        Plan plan = new Plan();

        assertFalse(plan.isFamilyShared());
        assertNull(plan.getPlanId());
        assertNull(plan.getPlanName());
        assertNull(plan.getMonthlyRental());
        assertNull(plan.getDataAllowanceGB());
        assertNull(plan.getVoiceAllowedMins());
        assertNull(plan.getSmsAllowed());
        assertNull(plan.getDataOverageRatePerGB());
        assertNull(plan.getVoiceOverageRatePerMin());
        assertNull(plan.getSmsOveragePerSMS());
        assertNull(plan.getWeekendFreeMinutes());

        plan.setPlanId("TEST-01");
        plan.setPlanName("Test Plan");
        plan.setMonthlyRental(123.45);
        plan.setDataAllowanceGB(10.0);
        plan.setVoiceAllowedMins(20.0);
        plan.setSmsAllowed(30);
        plan.setDataOverageRatePerGB(1.1);
        plan.setVoiceOverageRatePerMin(2.2);
        plan.setSmsOveragePerSMS(3.3);
        plan.setWeekendFreeMinutes(40.0);
        plan.setFamilyShared(true);

        assertEquals("TEST-01", plan.getPlanId());
        assertEquals("Test Plan", plan.getPlanName());
        assertEquals(123.45, plan.getMonthlyRental());
        assertEquals(10.0, plan.getDataAllowanceGB());
        assertEquals(20.0, plan.getVoiceAllowedMins());
        assertEquals(30, plan.getSmsAllowed());
        assertEquals(1.1, plan.getDataOverageRatePerGB());
        assertEquals(2.2, plan.getVoiceOverageRatePerMin());
        assertEquals(3.3, plan.getSmsOveragePerSMS());
        assertEquals(40.0, plan.getWeekendFreeMinutes());
        assertTrue(plan.isFamilyShared());
    }

    @Test
    void planConstantsHaveExpectedValues() {
        assertEquals(0.18, PlanConfig.GST_RATE);
        assertEquals(0.05, PlanConfig.REFERRAL_DISCOUNT_RATE);
        assertEquals(0.5, PlanConfig.NIGHT_TIME_DATA_DISCOUNT_WEIGHT);
        assertEquals(0.15, PlanConfig.DOMESTIC_ROAMING_SURCHARGE_RATE);
        assertEquals(0.60, PlanConfig.FAMILY_FAIRNESS_USAGE_THRESHOLD);
        assertEquals(250.0, PlanConfig.FAMILY_FAIRNESS_SURCHARGE);
        assertEquals(0.50, PlanConfig.DATA_ROLLOVER_CAP_PERCENT);
        assertEquals(60, PlanConfig.CREDIT_CONTROL_DAYS_THRESHOLD);
    }
}
