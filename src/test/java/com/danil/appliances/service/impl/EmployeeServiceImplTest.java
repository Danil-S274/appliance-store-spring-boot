package com.danil.appliances.service.impl;

import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.model.Employee;
import com.danil.appliances.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock EmployeeRepository employeeRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks EmployeeServiceImpl service;

    @Test
    void setEnabled_cannotDisableSelf_throws() {
        Employee e = new Employee();
        e.setId(1L);
        e.setEmail("test@test.com");

        when(this.employeeRepository.findById(1L)).thenReturn(Optional.of(e));

        assertThatThrownBy(() -> this.service.setEnabled(1L, false, "test@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("disable your own account");

        verify(this.employeeRepository, never()).save(any());
    }

    @Test
    void setPassword_blank_throws() {
        assertThatThrownBy(() -> this.service.setPassword(1L, "  "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password is required");
    }

    @Test
    void changeMyPassword_currentMismatch_throws() {
        Employee e = new Employee();
        e.setEmail("test@test.com");
        e.setPassword("HASH");

        when(this.employeeRepository.findByEmail("test@test.com")).thenReturn(Optional.of(e));
        when(this.passwordEncoder.matches("bad", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> this.service.changeMyPassword("test@test.com", "bad", "new"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("incorrect");

        verify(this.employeeRepository, never()).save(any());
    }

    @Test
    void changeMyPassword_ok_encodesAndSaves() {
        Employee e = new Employee();
        e.setEmail("test@test.com");
        e.setPassword("HASH");

        when(this.employeeRepository.findByEmail("test@test.com")).thenReturn(Optional.of(e));
        when(this.passwordEncoder.matches("ok", "HASH")).thenReturn(true);
        when(this.passwordEncoder.encode("new")).thenReturn("NEW_HASH");

        this.service.changeMyPassword("test@test.com", "ok", "new");

        assertThat(e.getPassword()).isEqualTo("NEW_HASH");
        verify(this.employeeRepository).save(e);
    }

    @Test
    void getByEmail_notFound_throws() {
        when(this.employeeRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.service.getByEmail("test@test.com"))
                .isInstanceOf(NotFoundException.class);
    }
}
