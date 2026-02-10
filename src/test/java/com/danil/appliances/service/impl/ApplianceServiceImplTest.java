package com.danil.appliances.service.impl;

import com.danil.appliances.dto.appliance.ApplianceDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.mapper.ApplianceMapper;
import com.danil.appliances.model.Appliance;
import com.danil.appliances.model.Manufacturer;
import com.danil.appliances.repository.ApplianceRepository;
import com.danil.appliances.repository.ManufacturerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplianceServiceImplTest {

    @Mock ApplianceRepository applianceRepository;
    @Mock ManufacturerRepository manufacturerRepository;
    @Mock ApplianceMapper applianceMapper;

    @InjectMocks ApplianceServiceImpl service;

    @Test
    void findAll_delegatesToRepo() {
        when(this.applianceRepository.findAll()).thenReturn(List.of(new Appliance()));

        List<Appliance> result = this.service.findAll();

        assertThat(result).hasSize(1);
        verify(this.applianceRepository).findAll();
    }

    @Test
    void findById_notFound() {
        when(this.applianceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.service.findById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_manufacturerNotFound_throws() {
        ApplianceDto dto = mock(ApplianceDto.class);
        when(dto.getManufacturerId()).thenReturn(7L);
        when(this.manufacturerRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.service.create(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Manufacturer not found");

        verify(applianceRepository, never()).save(any());
    }

    @Test
    void create_ok_maps_setsManufacturer_saves() {
        ApplianceDto dto = mock(ApplianceDto.class);
        when(dto.getManufacturerId()).thenReturn(7L);

        Manufacturer m = new Manufacturer();
        m.setId(7L);
        when(this.manufacturerRepository.findById(7L)).thenReturn(Optional.of(m));

        when(this.applianceRepository.save(any(Appliance.class))).thenAnswer(inv -> {
            Appliance a = inv.getArgument(0);
            a.setId(123L);
            return a;
        });

        Appliance saved = this.service.create(dto);

        assertThat(saved.getId()).isEqualTo(123L);
        assertThat(saved.getManufacturer()).isSameAs(m);
        verify(this.applianceMapper).updateEntity(eq(dto), any(Appliance.class));
        verify(this.applianceRepository).save(any(Appliance.class));
    }

    @Test
    void update_applianceNotFound_throws() {
        ApplianceDto dto = mock(ApplianceDto.class);
        when(dto.getId()).thenReturn(1L);

        when(this.applianceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.service.update(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Appliance not found");
    }

    @Test
    void delete_notExists_throws() {
        when(this.applianceRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> this.service.delete(1L))
                .isInstanceOf(NotFoundException.class);

        verify(this.applianceRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_integrityViolation_wrapsBusinessException() {
        when(this.applianceRepository.existsById(1L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(this.applianceRepository).deleteById(1L);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("used in orders");
    }

    @Test
    void delete_ok_callsDeleteAndFlush() {
        when(this.applianceRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(this.applianceRepository).deleteById(1L);
        verify(this.applianceRepository).flush();
    }
}
