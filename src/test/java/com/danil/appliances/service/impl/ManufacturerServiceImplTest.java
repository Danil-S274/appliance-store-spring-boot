package com.danil.appliances.service.impl;

import com.danil.appliances.dto.appliance.ManufacturerDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.repository.ApplianceRepository;
import com.danil.appliances.repository.ManufacturerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManufacturerServiceImplTest {

    @Mock ManufacturerRepository manufacturerRepository;
    @Mock ApplianceRepository applianceRepository;

    @InjectMocks ManufacturerServiceImpl service;

    @Test
    void create_nameExists_throws() {
        ManufacturerDto dto = mock(ManufacturerDto.class);
        when(dto.getName()).thenReturn("Samsung");
        when(manufacturerRepository.existsByName("Samsung")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void update_notFound_throws() {
        ManufacturerDto dto = mock(ManufacturerDto.class);
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_hasLinkedAppliances_throws() {
        when(applianceRepository.existsByManufacturerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("appliances linked");
    }

    @Test
    void delete_ok_deletesById() {
        when(applianceRepository.existsByManufacturerId(1L)).thenReturn(false);

        service.delete(1L);

        verify(manufacturerRepository).deleteById(1L);
    }
}
