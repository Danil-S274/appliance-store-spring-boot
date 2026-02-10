package com.danil.appliances.service.impl;

import com.danil.appliances.dto.user.ClientCreateDto;
import com.danil.appliances.dto.user.ClientUpdateDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.mapper.ClientMapper;
import com.danil.appliances.model.Client;
import com.danil.appliances.repository.ClientRepository;
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
class ClientServiceImplTest {

    @Mock ClientRepository clientRepository;
    @Mock ClientMapper clientMapper;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks ClientServiceImpl service;

    @Test
    void create_emailExists_throws() {
        ClientCreateDto dto = mock(ClientCreateDto.class);
        when(dto.getEmail()).thenReturn("test@test.com");
        when(this.clientRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> this.service.create(dto))
                .isInstanceOf(BusinessException.class);

        verify(this.clientRepository, never()).save(any());
    }

    @Test
    void create_invalidCard_throws() {
        ClientCreateDto dto = mock(ClientCreateDto.class);
        when(dto.getEmail()).thenReturn("test@test.com");
        when(dto.getName()).thenReturn("  Name  ");
        when(dto.getPassword()).thenReturn("pass");
        when(dto.getCardNumber()).thenReturn("aasddas");

        when(this.clientRepository.existsByEmail("test@test.com")).thenReturn(false);

        Client mapped = new Client();
        when(this.clientMapper.toEntity(dto)).thenReturn(mapped);

        when(this.passwordEncoder.encode("pass")).thenReturn("HASH");

        assertThatThrownBy(() -> this.service.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("13-19 digits");

        verify(this.clientRepository, never()).save(any());
    }

    @Test
    void create_ok_setsTrimmedName_encodedPassword_cardFields_andSaves() {
        ClientCreateDto dto = mock(ClientCreateDto.class);
        when(dto.getEmail()).thenReturn("test@test.com");
        when(dto.getName()).thenReturn("  Name  ");
        when(dto.getPassword()).thenReturn("pass");
        when(dto.getCardNumber()).thenReturn("4111 1111 1111 1111");

        when(this.clientRepository.existsByEmail("test@test.com")).thenReturn(false);

        Client mapped = new Client();
        when(this.clientMapper.toEntity(dto)).thenReturn(mapped);

        when(this.passwordEncoder.encode("pass")).thenReturn("HASH");
        when(this.clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        Client saved = this.service.create(dto);

        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getName()).isEqualTo("Name");
        assertThat(saved.getPassword()).isEqualTo("HASH");
        assertThat(saved.getCardLast4()).isEqualTo("1111");
        assertThat(saved.getCardHash()).hasSize(64);
        verify(this.clientRepository).save(mapped);
    }

    @Test
    void update_notFound_throws() {
        when(this.clientRepository.findById(1L)).thenReturn(Optional.empty());
        ClientUpdateDto dto = mock(ClientUpdateDto.class);

        assertThatThrownBy(() -> this.service.update(1L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setEnabled_ok_saves() {
        Client c = new Client();
        c.setId(1L);
        c.setEnabled(true);

        when(this.clientRepository.findById(1L)).thenReturn(Optional.of(c));

        this.service.setEnabled(1L, false);

        assertThat(c.isEnabled()).isFalse();
        verify(this.clientRepository).save(c);
    }
}
