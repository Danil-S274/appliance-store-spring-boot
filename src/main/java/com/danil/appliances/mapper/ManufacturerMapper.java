package com.danil.appliances.mapper;

import com.danil.appliances.config.MapstructConfig;
import com.danil.appliances.dto.ManufacturerDto;
import com.danil.appliances.model.Manufacturer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapstructConfig.class)
public interface ManufacturerMapper {

    ManufacturerDto toDto(Manufacturer entity);

    Manufacturer toEntity(ManufacturerDto dto);

    void updateEntity(ManufacturerDto dto, @MappingTarget Manufacturer entity);
}

