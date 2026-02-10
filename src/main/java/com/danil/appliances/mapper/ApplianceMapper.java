package com.danil.appliances.mapper;

import com.danil.appliances.config.MapstructConfig;
import com.danil.appliances.dto.appliance.ApplianceDto;
import com.danil.appliances.model.Appliance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapstructConfig.class)
public interface ApplianceMapper {

    @Mapping(target = "manufacturerId", source = "manufacturer.id")
    @Mapping(target = "manufacturerName", source = "manufacturer.name")
    ApplianceDto toDto(Appliance entity);

    @Mapping(target = "manufacturer", ignore = true)
    Appliance toEntity(ApplianceDto dto);

    @Mapping(target = "manufacturer", ignore = true)
    void updateEntity(ApplianceDto dto, @MappingTarget Appliance entity);
}

