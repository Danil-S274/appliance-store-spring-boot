package com.danil.appliances.dto.user;

import lombok.Data;

@Data
public class EmployeeDto {

    private Long id;

    private String name;

    private String email;

    private String department;

    private Boolean enabled;
}
