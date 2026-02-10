package com.danil.appliances.service;

import com.danil.appliances.dto.user.EmployeeCreateDto;
import com.danil.appliances.dto.user.EmployeeUpdateDto;
import com.danil.appliances.model.Employee;

import java.util.List;

public interface EmployeeService {

    List<Employee> findAll();

    Employee findById(Long id);

    Employee create(EmployeeCreateDto dto);

    Employee update(Long id, EmployeeUpdateDto dto);

    void delete(Long id);

    void setEnabled(Long id, boolean enabled, String email);

    void setPassword(Long id, String rawPassword);

    Employee getByEmail(String email);

    void updateMyProfile(String email, EmployeeUpdateDto dto);

    void changeMyPassword(String email, String currentPassword, String newPassword);
}
