package com.danil.appliances.service.impl;

import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.model.*;
import com.danil.appliances.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceImplTest {

    @Mock
    OrdersRepository ordersRepository;
    @Mock
    OrderRowRepository orderRowRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    ClientRepository clientRepository;
    @Mock
    ApplianceRepository applianceRepository;

    @InjectMocks
    OrderCommandServiceImpl service;

    private Client client;
    private Orders draft;

    @BeforeEach
    void init() {
        client = new Client();
        client.setId(1L);
        client.setEmail("test@test.com");
        client.setBalance(new BigDecimal("200.00"));

        draft = new Orders();
        draft.setId(10L);
        draft.setClient(client);
        draft.setOrderStatus(OrderStatus.DRAFT);
        draft.setRows(new LinkedHashSet<>());
    }

    @Test
    void getOrCreateDraft_existingDraft_returnsIt() {
        when(this.clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(client));
        when(this.ordersRepository.findByClientIdAndOrderStatus(1L, OrderStatus.DRAFT)).thenReturn(Optional.of(draft));

        Orders got = this.service.getOrCreateDraft("test@test.com");

        assertThat(got).isSameAs(draft);
        verify(this.ordersRepository, never()).save(any());
    }

    @Test
    void getOrCreateDraft_noDraft_createsAndSaves() {
        when(this.clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(client));
        when(this.ordersRepository.findByClientIdAndOrderStatus(1L, OrderStatus.DRAFT)).thenReturn(Optional.empty());
        when(this.ordersRepository.save(any(Orders.class))).thenAnswer(inv -> {
            Orders o = inv.getArgument(0);
            o.setId(99L);
            return o;
        });

        Orders got = this.service.getOrCreateDraft("test@test.com");

        assertThat(got.getId()).isEqualTo(99L);
        assertThat(got.getOrderStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(got.getClient()).isSameAs(client);
        assertThat(got.getEmployee()).isNull();
    }

    @Test
    void addItemToDraft_quantityInvalid_throwsIAE() {
        assertThatThrownBy(() -> this.service.addItemToDraft("test@test.com", 5L, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addItemToDraft_createsNewRow_andSavesWithUpdatedAmount() {
        when(this.clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(client));
        when(this.ordersRepository.findByClientIdAndOrderStatus(1L, OrderStatus.DRAFT)).thenReturn(Optional.of(draft));
        when(this.ordersRepository.findWithRowsById(10L)).thenReturn(Optional.of(draft));

        Appliance a = new Appliance();
        a.setId(5L);
        a.setPrice(new BigDecimal("10.00"));
        when(this.applianceRepository.findById(5L)).thenReturn(Optional.of(a));

        when(this.orderRowRepository.findByOrdersIdAndApplianceId(10L, 5L)).thenReturn(Optional.empty());

        this.service.addItemToDraft("test@test.com", 5L, 3);

        verify(this.orderRowRepository).save(argThat(r ->
                r.getOrders().getId().equals(10L) &&
                        r.getAppliance().getId().equals(5L) &&
                        r.getNumber() == 3L &&
                        r.getAmount().compareTo(new BigDecimal("30.00")) == 0
        ));
    }

    @Test
    void checkout_emptyCart_throws() {
        when(this.clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(client));
        when(this.ordersRepository.findByClientIdAndOrderStatus(1L, OrderStatus.DRAFT)).thenReturn(Optional.of(draft));
        when(this.ordersRepository.findWithRowsById(10L)).thenReturn(Optional.of(draft)); // rows empty

        assertThatThrownBy(() -> this.service.checkout("test@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void checkout_notEnoughFunds_throws() {
        OrderRow r1 = new OrderRow();
        r1.setAmount(new BigDecimal("150.00"));
        OrderRow r2 = new OrderRow();
        r2.setAmount(new BigDecimal("150.00"));
        draft.setRows(new LinkedHashSet<>(Set.of(r1, r2)));

        client.setBalance(new BigDecimal("100.00"));

        when(this.clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(client));
        when(this.ordersRepository.findByClientIdAndOrderStatus(1L, OrderStatus.DRAFT)).thenReturn(Optional.of(draft));
        when(this.ordersRepository.findWithRowsById(10L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> this.service.checkout("test@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Not enough funds");
    }

    @Test
    void checkout_ok_deductsBalance_andSetsStatusNew() {
        OrderRow r1 = new OrderRow();
        r1.setAmount(new BigDecimal("30.00"));
        draft.setRows(new LinkedHashSet<>(Set.of(r1)));

        when(this.clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(client));
        when(this.ordersRepository.findByClientIdAndOrderStatus(1L, OrderStatus.DRAFT)).thenReturn(Optional.of(draft));
        when(this.ordersRepository.findWithRowsById(10L)).thenReturn(Optional.of(draft));

        when(this.clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));
        when(this.ordersRepository.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));

        Orders saved = this.service.checkout("test@test.com");

        assertThat(client.getBalance()).isEqualByComparingTo("170.00");
        assertThat(saved.getOrderStatus()).isEqualTo(OrderStatus.NEW);
        verify(this.clientRepository).save(client);
        verify(this.ordersRepository).save(draft);
    }

    @Test
    void approve_onlyNewAllowed_otherwiseThrows() {
        Orders o = new Orders();
        o.setId(1L);
        o.setOrderStatus(OrderStatus.DRAFT);

        when(this.ordersRepository.findById(1L)).thenReturn(Optional.of(o));

        Authentication auth = mock(Authentication.class); // можно вообще mock без when

        assertThatThrownBy(() -> this.service.approve(1L, auth))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only NEW");

        verify(this.employeeRepository, never()).findByEmail(anyString());
        verify(this.ordersRepository, never()).save(any());
    }


    @Test
    void approve_ok_setsEmployeeAndApproved() {
        Orders o = new Orders();
        o.setId(1L);
        o.setOrderStatus(OrderStatus.NEW);

        when(this.ordersRepository.findById(1L)).thenReturn(Optional.of(o));

        Employee emp = new Employee();
        emp.setId(7L);
        emp.setEmail("e@e.com");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@test.com");

        when(this.employeeRepository.findByEmail("test@test.com")).thenReturn(Optional.of(emp));
        when(this.ordersRepository.save(any(Orders.class))).thenAnswer(inv -> inv.getArgument(0));

        Orders saved = this.service.approve(1L, auth);

        assertThat(saved.getOrderStatus()).isEqualTo(OrderStatus.APPROVED);
        assertThat(saved.getEmployee()).isSameAs(emp);
    }
}
