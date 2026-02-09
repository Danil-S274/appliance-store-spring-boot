package com.danil.appliances.service.impl;

import com.danil.appliances.dto.ApplianceDto;
import com.danil.appliances.dto.ApplianceSearchFilter;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.mapper.ApplianceMapper;
import com.danil.appliances.model.Appliance;
import com.danil.appliances.model.Manufacturer;
import com.danil.appliances.repository.ApplianceRepository;
import com.danil.appliances.repository.ManufacturerRepository;
import com.danil.appliances.repository.spec.ApplianceSpecifications;
import com.danil.appliances.service.ApplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ApplianceServiceImpl implements ApplianceService {

    private final ApplianceRepository applianceRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ApplianceMapper applianceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Appliance> findAll() {
        return this.applianceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Appliance findById(Long id) {
        return this.applianceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appliance not found: id=%d".formatted(id)));
    }

    @Override
    public Appliance create(ApplianceDto dto) {
        Manufacturer manufacturer = this.manufacturerRepository.findById(dto.getManufacturerId())
                .orElseThrow(() -> new NotFoundException("Manufacturer not found: id=%d".formatted(dto.getManufacturerId())));

        Appliance appliance = new Appliance();
        this.applianceMapper.updateEntity(dto, appliance);
        appliance.setManufacturer(manufacturer);

        Appliance saved = this.applianceRepository.save(appliance);
        log.info("Created appliance id={}, name={}, manufacturerId={}", saved.getId(), saved.getName(), manufacturer.getId());
        return saved;
    }

    @Override
    public Appliance update(ApplianceDto dto) {
        Appliance appliance = this.applianceRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("Appliance not found: id=%d".formatted(dto.getId())));

        Manufacturer manufacturer = this.manufacturerRepository.findById(dto.getManufacturerId())
                .orElseThrow(() -> new NotFoundException("Manufacturer not found: id=%d".formatted(dto.getManufacturerId())));

        this.applianceMapper.updateEntity(dto, appliance);
        appliance.setManufacturer(manufacturer);

        Appliance saved = this.applianceRepository.save(appliance);
        log.info("Updated appliance id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public void delete(Long id) {
        if (!this.applianceRepository.existsById(id)) {
            throw new NotFoundException("Appliance not found: id=%d".formatted(id));
        }

        try {
            this.applianceRepository.deleteById(id);
            this.applianceRepository.flush(); // важно
            log.info("Deleted appliance id={}", id);

        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Cannot delete appliance because it is used in orders");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appliance> search(ApplianceSearchFilter filter, Pageable pageable) {
        return this.applianceRepository.findAll(ApplianceSpecifications.byFilter(filter), pageable);
    }
}

