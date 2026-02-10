package com.danil.appliances.service.impl;

import com.danil.appliances.dto.account.AccountUpdateDto;
import com.danil.appliances.dto.account.ChangePasswordDto;
import com.danil.appliances.dto.account.UpdateCardDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.model.Client;
import com.danil.appliances.model.OrderStatus;
import com.danil.appliances.model.Orders;
import com.danil.appliances.repository.ClientRepository;
import com.danil.appliances.repository.OrdersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock OrdersRepository ordersRepository;
    @Mock ClientRepository clientRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AccountServiceImpl service;

    private static final String EMAIL = "test@test.com";

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(10L);
        client.setEmail(EMAIL);
        client.setName("Test");
        client.setPassword("HASHED");
        client.setBalance(new BigDecimal("50.00"));
    }

    @Test
    void getClient_ok() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        Client result = this.service.getClient(EMAIL);

        assertThat(result).isSameAs(client);
    }

    @Test
    void getClient_notFound() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.service.getClient(EMAIL))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Client not found");
    }

    @Test
    void getBalance_nullBalance_returnsZero() {
        client.setBalance(null);
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        BigDecimal balance = this.service.getBalance(EMAIL);

        assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void updateProfile_trimsName_andSaves() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(this.clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountUpdateDto dto = mock(AccountUpdateDto.class);
        when(dto.getName()).thenReturn("  New Name  ");

        Client saved = this.service.updateProfile(EMAIL, dto);

        assertThat(saved.getName()).isEqualTo("New Name");
        verify(this.clientRepository).save(client);
    }

    @Test
    void updateCard_normalizesSpaces_setsLast4_andHash() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(this.clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateCardDto dto = mock(UpdateCardDto.class);
        when(dto.getCardNumber()).thenReturn("  4111 1111 1111 1111  ");

        Client saved = this.service.updateCard(EMAIL, dto);

        assertThat(saved.getCardLast4()).isEqualTo("1111");
        assertThat(saved.getCardHash()).isNotBlank();
        assertThat(saved.getCardHash()).hasSize(64); // sha256 hex
        verify(this.clientRepository).save(client);
    }

    @Test
    void updateCard_invalidDigits_throwsBusinessException() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        UpdateCardDto dto = mock(UpdateCardDto.class);
        when(dto.getCardNumber()).thenReturn("asdcd");

        assertThatThrownBy(() -> this.service.updateCard(EMAIL, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("13-19 digits");

        verify(this.clientRepository, never()).save(any());
    }

    @Test
    void changeClientPassword_currentPasswordMismatch_throws() {
        when(clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        ChangePasswordDto dto = mock(ChangePasswordDto.class);
        when(dto.getCurrentPassword()).thenReturn("wrong");

        when(this.passwordEncoder.matches("wrong", "HASHED")).thenReturn(false);

        assertThatThrownBy(() -> this.service.changeClientPassword(EMAIL, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(this.clientRepository, never()).save(any());
        verify(dto, never()).getNewPassword();
        verify(dto, never()).getConfirmNewPassword();
    }


    @Test
    void changeClientPassword_confirmMismatch_throws() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        ChangePasswordDto dto = mock(ChangePasswordDto.class);
        when(dto.getCurrentPassword()).thenReturn("ok");
        when(dto.getNewPassword()).thenReturn("new1");
        when(dto.getConfirmNewPassword()).thenReturn("new2");

        when(this.passwordEncoder.matches("ok", "HASHED")).thenReturn(true);

        assertThatThrownBy(() -> service.changeClientPassword(EMAIL, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Passwords do not match");

        verify(this.clientRepository, never()).save(any());
    }

    @Test
    void changeClientPassword_ok_encodesAndSaves() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        ChangePasswordDto dto = mock(ChangePasswordDto.class);
        when(dto.getCurrentPassword()).thenReturn("ok");
        when(dto.getNewPassword()).thenReturn("new");
        when(dto.getConfirmNewPassword()).thenReturn("new");

        when(this.passwordEncoder.matches("ok", "HASHED")).thenReturn(true);
        when(this.passwordEncoder.encode("new")).thenReturn("NEW_HASH");

        service.changeClientPassword(EMAIL, dto);

        assertThat(client.getPassword()).isEqualTo("NEW_HASH");
        verify(clientRepository).save(client);
    }

    @Test
    void topUpBalance_invalidAmount_throws() {
        assertThatThrownBy(() -> this.service.topUpBalance(EMAIL, BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("> 0");

        verifyNoInteractions(this.clientRepository);
    }

    @Test
    void topUpBalance_ok_updatesBalance_andReturnsUpdated() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));

        BigDecimal updated = this.service.topUpBalance(EMAIL, new BigDecimal("20.50"));

        assertThat(updated).isEqualByComparingTo("70.50");
        verify(this.clientRepository).save(client);
    }

    @Test
    void deleteAccount_hasNewOrders_throws() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(this.ordersRepository.existsByClientIdAndOrderStatus(10L, OrderStatus.NEW)).thenReturn(true);

        assertThatThrownBy(() -> this.service.deleteAccount(EMAIL))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NEW orders");

        verify(this.clientRepository, never()).save(any());
        verify(this.ordersRepository, never()).delete(any());
    }

    @Test
    void deleteAccount_deletesDraftIfExists_disablesClient() {
        when(this.clientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(client));
        when(this.ordersRepository.existsByClientIdAndOrderStatus(10L, OrderStatus.NEW)).thenReturn(false);

        Orders draft = new Orders();
        draft.setId(99L);

        when(this.ordersRepository.findByClientIdAndOrderStatus(10L, OrderStatus.DRAFT))
                .thenReturn(Optional.of(draft));

        this.service.deleteAccount(EMAIL);

        verify(this.ordersRepository).delete(draft);
        assertThat(client.isEnabled()).isFalse();
        verify(this.clientRepository).save(client);
    }
}
