package com.danil.appliances.service;

import com.danil.appliances.dto.appliance.ApplianceDto;
import com.danil.appliances.dto.appliance.ApplianceSearchFilter;
import com.danil.appliances.model.Appliance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApplianceService {

    List<Appliance> findAll();

    Appliance findById(Long id);

    Appliance create(ApplianceDto dto);

    Appliance update(ApplianceDto dto);

    void delete(Long id);

    Page<Appliance> search(ApplianceSearchFilter filter, Pageable pageable);
}
