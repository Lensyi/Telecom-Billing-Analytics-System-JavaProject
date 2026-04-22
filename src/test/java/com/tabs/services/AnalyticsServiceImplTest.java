package com.tabs.services;

import com.tabs.dao.BillingDAO;
import com.tabs.dao.CustomerDAO;
import com.tabs.dao.SubscriptionDAO;
import com.tabs.dao.UsageDAO;
import com.tabs.models.Customer;
import com.tabs.models.Invoice;
import com.tabs.models.Plan;
import com.tabs.models.Subscription;
import com.tabs.models.Usage;
import com.tabs.utility.PlanConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AnalyticsServiceImplTest {

    private UsageDAO usageDAO;
    private BillingDAO billingDAO;
    private SubscriptionDAO subscriptionDAO;
    private CustomerDAO customerDAO;
    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        usageDAO = Mockito.mock(UsageDAO.class);
        billingDAO = Mockito.mock(BillingDAO.class);
        subscriptionDAO = Mockito.mock(SubscriptionDAO.class);
        customerDAO = Mockito.mock(CustomerDAO.class);
        analyticsService = new AnalyticsServiceImpl(usageDAO, billingDAO, subscriptionDAO, customerDAO);
    }

    @Test
    void getSystemArpu_returnsZeroWhenNoInvoices() {
        when(billingDAO.getAllInvoices()).thenReturn(Collections.emptyList());

        double arpu = analyticsService.getSystemArpu();

        assertEquals(0.0, arpu, 0.0000001);
    }

    @Test
    void getSystemArpu_averagesGrandTotalsAcrossInvoices() {
        Invoice invoice1 = Mockito.mock(Invoice.class);
        Invoice invoice2 = Mockito.mock(Invoice.class);
        Invoice invoice3 = Mockito.mock(Invoice.class);
        when(invoice1.getGrandTotal()).thenReturn(100.0);
        when(invoice2.getGrandTotal()).thenReturn(200.0);
        when(invoice3.getGrandTotal()).thenReturn(50.0);
        when(billingDAO.getAllInvoices()).thenReturn(List.of(invoice1, invoice2, invoice3));

        double arpu = analyticsService.getSystemArpu();

        assertEquals(116.6666666667, arpu, 0.0000001);
    }

    @Test
    void getOverageDistribution_returnsZeroSummaryForEmptyInvoices() {
        when(billingDAO.getAllInvoices()).thenReturn(Collections.emptyList());

        DoubleSummaryStatistics stats = analyticsService.getOverageDistribution();

        assertNotNull(stats);
        assertEquals(0L, stats.getCount());
        assertEquals(0.0, stats.getSum(), 0.0000001);
        assertEquals(0.0, stats.getAverage(), 0.0000001);
        assertEquals(0.0, stats.getMin(), 0.0000001);
        assertEquals(0.0, stats.getMax(), 0.0000001);
    }

    @Test
    void getOverageDistribution_ignoresNullOverageFareAndSummarizesValidValues() {
        Invoice invoice1 = Mockito.mock(Invoice.class);
        Invoice invoice2 = Mockito.mock(Invoice.class);
        Invoice invoice3 = Mockito.mock(Invoice.class);
        when(invoice1.getOverageFare()).thenReturn(10.5);
        when(invoice2.getOverageFare()).thenReturn(null);
        when(invoice3.getOverageFare()).thenReturn(4.5);
        when(billingDAO.getAllInvoices()).thenReturn(List.of(invoice1, invoice2, invoice3));

        DoubleSummaryStatistics stats = analyticsService.getOverageDistribution();

        assertEquals(2L, stats.getCount());
        assertEquals(15.0, stats.getSum(), 0.0000001);
        assertEquals(7.5, stats.getAverage(), 0.0000001);
        assertEquals(4.5, stats.getMin(), 0.0000001);
        assertEquals(10.5, stats.getMax(), 0.0000001);
    }

    @Test
    void getTopNDataUsers_returnsEmptyListWhenNoUsage() {
        when(usageDAO.getAllUsageBySubscription()).thenReturn(Collections.emptyMap());

        List<Map.Entry<String, Double>> result = analyticsService.getTopNDataUsers(5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTopNDataUsers_sumsPerSubscriptionAndReturnsTopN() {
        Usage u1 = Mockito.mock(Usage.class);
        Usage u2 = Mockito.mock(Usage.class);
        Usage u3 = Mockito.mock(Usage.class);
        when(u1.getDataUsedGB()).thenReturn(1.5);
        when(u2.getDataUsedGB()).thenReturn(2.0);
        when(u3.getDataUsedGB()).thenReturn(0.5);

        Map<String, List<Usage>> usageBySub = new HashMap<>();
        usageBySub.put("sub-1", List.of(u1, u2));
        usageBySub.put("sub-2", List.of(u3));
        when(usageDAO.getAllUsageBySubscription()).thenReturn(usageBySub);

        List<Map.Entry<String, Double>> result = analyticsService.getTopNDataUsers(1);

        assertEquals(1, result.size());
        assertEquals("sub-1", result.get(0).getKey());
        assertEquals(3.5, result.get(0).getValue(), 0.0000001);
    }

    @Test
    void getCreditRiskCustomers_returnsEmptyWhenNoPendingOverdueInvoices() {
        when(billingDAO.getAllInvoices()).thenReturn(Collections.emptyList());

        List<Customer> result = analyticsService.getCreditRiskCustomers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCreditRiskCustomers_returnsUniqueCustomersForOverduePendingInvoices() {
        Invoice overdue1 = Mockito.mock(Invoice.class);
        Invoice overdue2 = Mockito.mock(Invoice.class);
        Invoice paid = Mockito.mock(Invoice.class);
        when(overdue1.getPaymentStatus()).thenReturn(Invoice.PaymentStatus.PENDING);
        when(overdue2.getPaymentStatus()).thenReturn(Invoice.PaymentStatus.PENDING);
        when(paid.getPaymentStatus()).thenReturn(Invoice.PaymentStatus.PAID);

        when(overdue1.getBillingDate()).thenReturn(LocalDate.now().minusDays(PlanConfig.CREDIT_CONTROL_DAYS_THRESHOLD + 1L));
        when(overdue2.getBillingDate()).thenReturn(LocalDate.now().minusDays(PlanConfig.CREDIT_CONTROL_DAYS_THRESHOLD + 5L));
        when(paid.getBillingDate()).thenReturn(LocalDate.now().minusDays(PlanConfig.CREDIT_CONTROL_DAYS_THRESHOLD + 10L));

        when(overdue1.getCustId()).thenReturn("cust-1");
        when(overdue2.getCustId()).thenReturn("cust-1");
        when(paid.getCustId()).thenReturn("cust-2");
        when(billingDAO.getAllInvoices()).thenReturn(List.of(overdue1, overdue2, paid));

        Customer c1 = Mockito.mock(Customer.class);
        when(customerDAO.getCustomerById("cust-1")).thenReturn(c1);

        List<Customer> result = analyticsService.getCreditRiskCustomers();

        assertEquals(1, result.size());
        assertEquals(c1, result.get(0));
    }

    @Test
    void getPlanRecommendations_returnsRecommendationForCustomerWithNoUsageUsingAvailablePlans() {
        Customer customer = Mockito.mock(Customer.class);
        when(customer.getCustId()).thenReturn("cust-1");
        when(customer.getName()).thenReturn("Alice");
        when(customerDAO.getAllCustomers()).thenReturn(List.of(customer));
        when(subscriptionDAO.getSubscriptionsByCustomer("cust-1")).thenReturn(Collections.emptyList());

        Map<String, String> recommendations = analyticsService.getPlanRecommendations();

        assertEquals(1, recommendations.size());
        assertTrue(recommendations.containsKey("Alice"));
        assertTrue(recommendations.get("Alice").startsWith("Recommended Plan:"));
    }

    @Test
    void getPlanRecommendations_handlesPartialUsageDataWithoutThrowing() {
        Customer customer = Mockito.mock(Customer.class);
        Subscription subscription = Mockito.mock(Subscription.class);
        Usage usage = Mockito.mock(Usage.class);

        when(customer.getCustId()).thenReturn("cust-2");
        when(customer.getName()).thenReturn("Bob");
        when(subscription.getSubscriptionId()).thenReturn("sub-2");
        when(customerDAO.getAllCustomers()).thenReturn(List.of(customer));
        when(subscriptionDAO.getSubscriptionsByCustomer("cust-2")).thenReturn(List.of(subscription));
        when(usageDAO.getUsageBySubscriptionIdAndPeriod("sub-2", YearMonth.now().minusMonths(1))).thenReturn(List.of(usage));

        when(usage.getDataUsedGB()).thenReturn(null);
        when(usage.getVoiceUsedMins()).thenReturn(15.0);
        when(usage.getSmsUsed()).thenReturn(null);

        Map<String, String> recommendations = analyticsService.getPlanRecommendations();

        assertEquals(1, recommendations.size());
        assertTrue(recommendations.get("Bob").contains("Recommended Plan:"));
    }
}
