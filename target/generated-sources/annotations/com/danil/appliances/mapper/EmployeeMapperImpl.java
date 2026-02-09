package com.danil.appliances.mapper;

import com.danil.appliances.dto.EmployeeCreateDto;
import com.danil.appliances.dto.EmployeeDto;
import com.danil.appliances.dto.EmployeeUpdateDto;
import com.danil.appliances.model.Employee;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-09T13:36:21+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public Employee toEntity(EmployeeCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Employee employee = new Employee();

        employee.setName( dto.getName() );
        employee.setEmail( dto.getEmail() );
        employee.setDepartment( dto.getDepartment() );

        return employee;
    }

    @Override
    public void updateEntity(EmployeeUpdateDto dto, Employee entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getName() != null ) {
            entity.setName( dto.getName() );
        }
        if ( dto.getDepartment() != null ) {
            entity.setDepartment( dto.getDepartment() );
        }
    }

    @Override
    public EmployeeDto toDto(Employee entity) {
        if ( entity == null ) {
            return null;
        }

        EmployeeDto employeeDto = new EmployeeDto();

        employeeDto.setId( entity.getId() );
        employeeDto.setName( entity.getName() );
        employeeDto.setEmail( entity.getEmail() );
        employeeDto.setDepartment( entity.getDepartment() );
        employeeDto.setEnabled( entity.isEnabled() );

        return employeeDto;
    }
}
