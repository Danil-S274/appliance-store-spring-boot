package com.danil.appliances.service.impl;

import com.danil.appliances.dto.ManufacturerDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.mapper.ManufacturerMapper;
import com.danil.appliances.model.Manufacturer;
import com.danil.appliances.repository.ApplianceRepository;
import com.danil.appliances.repository.ManufacturerRepository;
import com.danil.appliances.service.ManufacturerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ApplianceRepository applianceRepository;
    private final ManufacturerMapper manufacturerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Manufacturer> findAll() {
        return this.manufacturerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Manufacturer findById(Long id) {
        return this.manufacturerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Manufacturer not found: id=%d".formatted(id)));
    }

    @Override
    public Manufacturer create(ManufacturerDto dto) {
        if (this.manufacturerRepository.existsByName(dto.getName())) {
            throw new BusinessException("Manufacturer with name already exists: %s".formatted(dto.getName()));
        }
        Manufacturer entity = this.manufacturerMapper.toEntity(dto);
        Manufacturer saved = this.manufacturerRepository.save(entity);
        log.info("Created manufacturer id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public Manufacturer update(Long id, ManufacturerDto dto) {
        Manufacturer entity = this.manufacturerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Manufacturer not found: id=%d".formatted(id)));

        String newName = dto.getName();
        if (!entity.getName().equalsIgnoreCase(newName) && this.manufacturerRepository.existsByName(newName)) {
            throw new BusinessException("Manufacturer with name already exists: %s".formatted(newName));
        }

        this.manufacturerMapper.updateEntity(dto, entity);
        Manufacturer saved = this.manufacturerRepository.save(entity);
        log.info("Updated manufacturer id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public void delete(Long id) {
        if (applianceRepository.existsByManufacturerId(id)) {
            throw new BusinessException("Can't delete manufacturer: there are appliances linked to it");
        }
        this.manufacturerRepository.deleteById(id);
        log.info("Deleted manufacturer id={}", id);
    }
}

