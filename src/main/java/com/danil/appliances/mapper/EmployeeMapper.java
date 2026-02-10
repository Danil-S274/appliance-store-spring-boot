package com.danil.appliances.mapper;

import com.danil.appliances.config.MapstructConfig;
import com.danil.appliances.dto.user.EmployeeCreateDto;
import com.danil.appliances.dto.user.EmployeeDto;
import com.danil.appliances.dto.user.EmployeeUpdateDto;
import com.danil.appliances.model.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapstructConfig.class)
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "provider", ignore = true)
    Employee toEntity(EmployeeCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "provider", ignore = true)
    void updateEntity(EmployeeUpdateDto dto, @MappingTarget Employee entity);

    EmployeeDto toDto(Employee entity);
}


