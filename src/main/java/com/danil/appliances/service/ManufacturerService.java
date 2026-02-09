package com.danil.appliances.service;

import com.danil.appliances.dto.ManufacturerDto;
import com.danil.appliances.model.Manufacturer;

import java.util.List;

public interface ManufacturerService {

    List<Manufacturer> findAll();

    Manufacturer findById(Long id);

    Manufacturer create(ManufacturerDto dto);

    Manufacturer update(Long id, ManufacturerDto dto);

    void delete(Long id);
}
