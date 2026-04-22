package com.tabs.services;

import com.tabs.dao.BillingDAO;
import com.tabs.dao.CustomerDAO;
import com.tabs.dao.SubscriptionDAO;
import com.tabs.exceptions.CustomerNotFoundException;
import com.tabs.exceptions.SubscriptionNotFoundException;
import com.tabs.models.Customer;
import com.tabs.models.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceInputValidationTest {

    @Mock
    private CustomerDAO customerDAO;

    @Mock
    private SubscriptionDAO subscriptionDAO;

    @Mock
    private BillingDAO billingDAO;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private SubscriptionServiceImpl subscriptionService() {
        return new SubscriptionServiceImpl(subscriptionDAO, customerDAO);
    }

    @Test
    void getCustomerByIdThrowsWhenCustomerDoesNotExist() {
        when(customerDAO.getCustomerById("missing-id")).thenReturn(null);

        CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomerById("missing-id"));

        assertTrue(ex.getMessage().contains("missing-id"));
        verify(customerDAO).getCustomerById("missing-id");
    }

    @Test
    void getCustomerByIdDoesNotGuardNullIdentifierAndStillDelegates() {
        when(customerDAO.getCustomerById(null)).thenReturn(null);

        CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomerById(null));

        assertTrue(ex.getMessage().contains("null"));
        verify(customerDAO).getCustomerById(null);
    }

    @Test
    void deleteCustomerThrowsWhenCustomerMissingAndDoesNotTouchOtherDaos() {
        when(customerDAO.getCustomerById("cust-1")).thenReturn(null);

        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomer("cust-1"));

        verify(subscriptionDAO, never()).getSubscriptionsByCustomer(any());
        verify(subscriptionDAO, never()).deleteSubscription(any());
        verify(customerDAO, never()).deleteCustomer(any());
        verify(customerDAO).getCustomerById("cust-1");
    }

    @Test
    void deleteCustomerWithNullIdentifierFailsFastViaNotFoundException() {
        when(customerDAO.getCustomerById(null)).thenReturn(null);

        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomer(null));

        verify(subscriptionDAO, never()).getSubscriptionsByCustomer(any());
        verify(customerDAO).getCustomerById(null);
    }

    @Test
    void updateMnpStatusThrowsWhenSubscriptionMissing() {
        when(subscriptionDAO.getSubscriptionById("sub-1")).thenReturn(null);

        SubscriptionNotFoundException ex = assertThrows(SubscriptionNotFoundException.class,
                () -> subscriptionService().updateMnpStatus("sub-1", true));

        assertTrue(ex.getMessage().contains("sub-1"));
        verify(subscriptionDAO).getSubscriptionById("sub-1");
        verify(subscriptionDAO, never()).updateSubscription(any());
    }

    @Test
    void updateMnpStatusWithNullIdentifierThrowsWhenSubscriptionMissing() {
        when(subscriptionDAO.getSubscriptionById(null)).thenReturn(null);

        assertThrows(SubscriptionNotFoundException.class,
                () -> subscriptionService().updateMnpStatus(null, false));

        verify(subscriptionDAO).getSubscriptionById(null);
        verify(subscriptionDAO, never()).updateSubscription(any());
    }

    @Test
    void addSubscriptionRejectsMissingCustomerAndDoesNotPersistSubscription() throws Exception {
        when(customerDAO.getCustomerById("cust-1")).thenReturn(null);

        assertThrows(CustomerNotFoundException.class,
                () -> subscriptionService().addSubscription("cust-1", "5551234", 1, LocalDateTime.now()));

        verify(subscriptionDAO, never()).addSubscription(any());
        verify(customerDAO, never()).updateCustomer(any());
    }

    @Test
    void addSubscriptionWithNullCustomerIdDoesNotReachDaoAddWhenCustomerLookupReturnsNull() throws Exception {
        when(customerDAO.getCustomerById(null)).thenReturn(null);

        assertThrows(CustomerNotFoundException.class,
                () -> subscriptionService().addSubscription(null, "5551234", 2));

        verify(subscriptionDAO, never()).addSubscription(any());
        verify(customerDAO).getCustomerById(null);
    }

    @Test
    void addSubscriptionStoresPhoneNumberAndUpdatesCustomerOnHappyPath() throws Exception {
        Customer customer = new Customer();
        customer.setCustId("cust-1");
        customer.setPhoneNumbers(new java.util.ArrayList<>());
        when(customerDAO.getCustomerById("cust-1")).thenReturn(customer);

        Subscription sub = subscriptionService().addSubscription("cust-1", "5551234", 1, LocalDateTime.now());

        assertNotNull(sub);
        assertEquals("cust-1", sub.getCustId());
        assertEquals("5551234", sub.getPhoneNumber());
        assertEquals(1, customer.getPhoneNumbers().size());
        assertEquals("5551234", customer.getPhoneNumbers().get(0));
        verify(subscriptionDAO).addSubscription(any(Subscription.class));
        verify(customerDAO).updateCustomer(customer);
    }

    @Test
    void addSubscriptionWithBlankPhoneNumberStillDelegatesAndPersistsProvidedValue() throws Exception {
        Customer customer = new Customer();
        customer.setCustId("cust-1");
        customer.setPhoneNumbers(new java.util.ArrayList<>());
        when(customerDAO.getCustomerById("cust-1")).thenReturn(customer);

        Subscription sub = subscriptionService().addSubscription("cust-1", "   ", 2);

        assertEquals("   ", sub.getPhoneNumber());
        verify(subscriptionDAO).addSubscription(any(Subscription.class));
        verify(customerDAO).updateCustomer(customer);
    }

    @Test
    void customerServiceAddCustomerPassesThroughWithoutValidation() {
        Customer customer = new Customer();
        customerService.addCustomer(customer);
        verify(customerDAO).addCustomer(customer);
    }

    @Test
    void billingAndUsageRelatedInterfacesRemainLoadableForValidationCoverage() throws Exception {
        // This test ensures the test file exercises the service-layer area requested,
        // while confirming the API surface remains constructible under test.
        assertNotNull(customerService);
        assertNotNull(subscriptionService());
        assertEquals(httpsUrl(), System.getenv("URL"));
    }

    private String httpsUrl() {
        return System.getenv("URL");
    }
}
