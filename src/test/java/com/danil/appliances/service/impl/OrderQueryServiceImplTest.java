package com.danil.appliances.service.impl;

import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.model.Client;
import com.danil.appliances.model.OrderStatus;
import com.danil.appliances.model.Orders;
import com.danil.appliances.repository.ClientRepository;
import com.danil.appliances.repository.OrdersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceImplTest {

    @Mock OrdersRepository ordersRepository;
    @Mock ClientRepository clientRepository;

    @InjectMocks OrderQueryServiceImpl service;

    @Test
    void listForClient_clientNotFound_throws() {
        when(this.clientRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listForClient("test@test.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getDetailsForClient_notOwner_denied() {
        Client c = new Client();
        c.setEmail("test@test.com");
        c.setId(1L);

        Client other = new Client();
        other.setEmail("other@test.com");
        other.setId(2L);

        Orders o = new Orders();
        o.setId(10L);
        o.setClient(other);

        when(this.ordersRepository.findWithRowsById(10L)).thenReturn(Optional.of(o));
        when(this.clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> this.service.getDetailsForClient(10L, "test@test.com"))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }


    @Test
    void getDraftForClient_noDraft_returnsNewTransientDraft() {
        Client c = new Client();
        c.setId(1L);
        c.setEmail("test@test.com");

        when(this.clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(c));
        when(this.ordersRepository.findByClientIdAndOrderStatus(1L, OrderStatus.DRAFT)).thenReturn(Optional.empty());

        Orders draft = this.service.getDraftForClient("test@test.com");

        assertThat(draft.getId()).isNull();
        assertThat(draft.getOrderStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(draft.getClient()).isSameAs(c);
    }

    @Test
    void getOrderForRead_employeeCanReadAny() {
        Orders o = new Orders();
        o.setId(1L);

        when(this.ordersRepository.findById(1L)).thenReturn(Optional.of(o));

        Authentication auth = mock(Authentication.class);
        when(auth.getAuthorities()).thenReturn(
                (Collection) Set.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );


        Orders got = this.service.getOrderForRead(1L, auth);

        assertThat(got).isSameAs(o);
    }
}
