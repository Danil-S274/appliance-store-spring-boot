package com.danil.appliances.service.impl;

import com.danil.appliances.dto.account.RegisterDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.model.Client;
import com.danil.appliances.repository.ClientRepository;
import com.danil.appliances.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock ClientRepository clientRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthServiceImpl service;

    @Test
    void registerClient_emailAlreadyExistsInEmployee_throws() {
        RegisterDto dto = mock(RegisterDto.class);
        when(dto.getEmail()).thenReturn(" Test@Test.com ");

        when(this.employeeRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerClient(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void registerClient_passwordMismatch_throws() {
        RegisterDto dto = mock(RegisterDto.class);
        when(dto.getEmail()).thenReturn("test@test.com");
        when(dto.getPassword()).thenReturn("p1");
        when(dto.getConfirmPassword()).thenReturn("p2");

        when(this.employeeRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(this.clientRepository.existsByEmail("test@test.com")).thenReturn(false);

        assertThatThrownBy(() -> this.service.registerClient(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    void registerClient_invalidCard_throws() {
        RegisterDto dto = mock(RegisterDto.class);
        when(dto.getEmail()).thenReturn("test@test.com");
        when(dto.getPassword()).thenReturn("p");
        when(dto.getConfirmPassword()).thenReturn("p");
        when(dto.getCardNumber()).thenReturn("11");

        when(this.employeeRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(this.clientRepository.existsByEmail("test@test.com")).thenReturn(false);

        assertThatThrownBy(() -> this.service.registerClient(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid card number");
    }

    @Test
    void registerClient_ok_normalizesEmail_setsCardLast4Hash_andDefaultBalance() {
        RegisterDto dto = mock(RegisterDto.class);
        when(dto.getEmail()).thenReturn("  Test@Test.com ");
        when(dto.getName()).thenReturn("  Danil ");
        when(dto.getPassword()).thenReturn("pass");
        when(dto.getConfirmPassword()).thenReturn("pass");
        when(dto.getCardNumber()).thenReturn("4111 1111 1111 1111");

        when(this.employeeRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(this.clientRepository.existsByEmail("test@test.com")).thenReturn(false);

        when(this.passwordEncoder.encode("pass")).thenReturn("HASH");

        when(this.clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client saved = this.service.registerClient(dto);

        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getName()).isEqualTo("Danil");
        assertThat(saved.getPassword()).isEqualTo("HASH");
        assertThat(saved.getCardLast4()).isEqualTo("1111");
        assertThat(saved.getCardHash()).hasSize(64);
        assertThat(saved.getBalance()).isEqualByComparingTo("0");
    }
}
