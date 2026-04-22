package com.tabs.dao;

import com.tabs.models.Customer;
import com.tabs.models.Invoice;
import com.tabs.models.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DAOEdgeCaseTest {

    @BeforeEach
    void resetSharedState() throws Exception {
        clearStaticMap(BillingDAOImpl.class, "invoices");
        clearStaticMap(CustomerDAOImpl.class, "customers");
    }

    @Test
    void billingAddInvoiceIgnoresNullAndMissingIdAndGettersReturnSafeDefaults() {
        BillingDAO billingDAO = new BillingDAOImpl();

        assertDoesNotThrow(() -> billingDAO.addInvoice(null));
        assertDoesNotThrow(() -> billingDAO.addInvoice(new Invoice()));

        assertNull(billingDAO.getInvoiceById(null));
        assertNull(billingDAO.getInvoiceById("missing"));
        assertTrue(billingDAO.getInvoicesByCustomer(null).isEmpty());
        assertTrue(billingDAO.getInvoicesBySubscriptionAndPeriod(null, YearMonth.of(2024, 1)).isEmpty());
        assertTrue(billingDAO.getInvoicesBySubscriptionAndPeriod("sub-1", null).isEmpty());
        assertTrue(billingDAO.getAllInvoices().isEmpty());
    }

    @Test
    void billingReturnsMatchingInvoiceAndFiltersBySubscriptionAndPeriod() {
        BillingDAO billingDAO = new BillingDAOImpl();

        Invoice invoice = new Invoice();
        invoice.setInvoiceId("inv-1");
        invoice.setCustId("cust-1");
        invoice.setSubscriptionId("sub-1");
        invoice.setBillingDate(LocalDate.of(2024, 1, 15));
        billingDAO.addInvoice(invoice);

        assertSame(invoice, billingDAO.getInvoiceById("inv-1"));
        assertEquals(1, billingDAO.getInvoicesByCustomer("cust-1").size());
        assertEquals(invoice, billingDAO.getInvoicesBySubscriptionAndPeriod("sub-1", YearMonth.of(2024, 1)).get(0));
        assertTrue(billingDAO.getInvoicesByCustomer("other").isEmpty());
        assertTrue(billingDAO.getInvoicesBySubscriptionAndPeriod("sub-1", YearMonth.of(2024, 2)).isEmpty());
    }

    @Test
    void billingUpdateIgnoresInvalidInputsAndReplacesExistingInvoice() {
        BillingDAO billingDAO = new BillingDAOImpl();

        Invoice original = new Invoice();
        original.setInvoiceId("inv-1");
        original.setCustId("cust-1");
        original.setSubscriptionId("sub-1");
        original.setBillingDate(LocalDate.of(2024, 1, 1));
        billingDAO.addInvoice(original);

        Invoice updated = new Invoice();
        updated.setInvoiceId("inv-1");
        updated.setCustId("cust-2");
        updated.setSubscriptionId("sub-2");
        updated.setBillingDate(LocalDate.of(2024, 2, 1));
        assertDoesNotThrow(() -> billingDAO.updateInvoice(updated));
        assertSame(updated, billingDAO.getInvoiceById("inv-1"));
        assertEquals(1, billingDAO.getAllInvoices().size());

        assertDoesNotThrow(() -> billingDAO.updateInvoice(null));
        assertEquals(1, billingDAO.getAllInvoices().size());

        Invoice noId = new Invoice();
        assertDoesNotThrow(() -> billingDAO.updateInvoice(noId));
        assertEquals(1, billingDAO.getAllInvoices().size());
    }

    @Test
    void customerAddGetDeleteHandleNullAndMissingInputsSafely() {
        CustomerDAO customerDAO = new CustomerDAOImpl();

        assertDoesNotThrow(() -> customerDAO.addCustomer(null));
        assertDoesNotThrow(() -> customerDAO.deleteCustomer(null));
        assertNull(customerDAO.getCustomerById(null));
        assertNull(customerDAO.getCustomerById("missing"));
        assertTrue(customerDAO.getAllCustomers().isEmpty());
    }

    @Test
    void customerUpdateReplacesExistingCustomerAndIgnoresInvalidInputs() {
        CustomerDAO customerDAO = new CustomerDAOImpl();

        Customer customer = new Customer();
        customer.setCustId("cust-1");
        customerDAO.addCustomer(customer);

        Customer updated = new Customer();
        updated.setCustId("cust-1");
        assertDoesNotThrow(() -> customerDAO.updateCustomer(updated));
        assertSame(updated, customerDAO.getCustomerById("cust-1"));

        assertDoesNotThrow(() -> customerDAO.updateCustomer(null));
        assertDoesNotThrow(() -> customerDAO.updateCustomer(new Customer()));
        assertSame(updated, customerDAO.getCustomerById("cust-1"));
    }

    @Test
    void planDaoInterfaceSupportsNullAndDoesNotForceImplementationBehavior() {
        PlanDAO planDAO = new PlanDAO() {
            @Override
            public Plan AddPlan(String PlanId) {
                return null;
            }
        };

        assertNull(planDAO.AddPlan(null));
        assertNull(planDAO.AddPlan("plan-1"));
    }

    private static void clearStaticMap(Class<?> type, String fieldName) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>) field.get(null);
        map.clear();
    }
}
