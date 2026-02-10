package com.danil.appliances.service.impl;

import com.danil.appliances.dto.user.EmployeeCreateDto;
import com.danil.appliances.dto.user.EmployeeUpdateDto;
import com.danil.appliances.exception.BusinessException;
import com.danil.appliances.exception.NotFoundException;
import com.danil.appliances.mapper.EmployeeMapper;
import com.danil.appliances.model.Employee;
import com.danil.appliances.repository.EmployeeRepository;
import com.danil.appliances.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final EmployeeMapper employeeMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return this.employeeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Employee findById(Long id) {
        return this.employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: id=%d".formatted(id)));
    }

    @Override
    public Employee create(EmployeeCreateDto dto) {
        if (this.employeeRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Employee with email already exists: %s".formatted(dto.getEmail()));
        }

        Employee employee = this.employeeMapper.toEntity(dto);
        employee.setEmail(dto.getEmail());
        employee.setName(dto.getName());
        employee.setDepartment(dto.getDepartment());
        employee.setPassword(this.passwordEncoder.encode(dto.getPassword()));

        Employee saved = this.employeeRepository.save(employee);
        log.info("Created Employee id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public Employee update(Long id, EmployeeUpdateDto dto) {
        Employee employee = this.employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: %d".formatted(id)));

        this.employeeMapper.updateEntity(dto, employee);
        employee.setName(dto.getName());
        employee.setDepartment(dto.getDepartment());
        Employee saved = this.employeeRepository.save(employee);
        log.info("Updated Employee id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public void delete(Long id) {
        if (!this.employeeRepository.existsById(id)) {
            throw new NotFoundException("Employee not found: %d".formatted(id));
        }

        this.employeeRepository.deleteById(id);
        log.info("Deleted employee id={}", id);
    }

    @Override
    public void setEnabled(Long id, boolean enabled, String email) {
        Employee employee = this.employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: %d".formatted(id)));

        if (employee.getEmail().equalsIgnoreCase(email) && !enabled) {
            throw new BusinessException("You cannot disable your own account");
        }
        employee.setEnabled(enabled);
        this.employeeRepository.save(employee);
        log.info("Employee id={} enabled={}", id, enabled);
    }

    @Override
    public void setPassword(Long id, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException("Password is required");
        }

        Employee employee = this.employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: %d".formatted(id)));
        employee.setPassword(rawPassword);
        this.employeeRepository.save(employee);
        log.info("Employee id={} password changed", id);
    }

    @Override
    public Employee getByEmail(String email) {
        return this.employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found: %s".formatted(email)));
    }

    @Override
    public void updateMyProfile(String email, EmployeeUpdateDto dto) {
        Employee employee = this.getByEmail(email);
        employee.setName(dto.getName());
        employee.setDepartment(dto.getDepartment());
        this.employeeRepository.save(employee);
    }

    @Override
    public void changeMyPassword(String email, String currentPassword, String newPassword) {
        Employee employee = this.getByEmail(email);
        if (!passwordEncoder.matches(currentPassword, employee.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        employee.setPassword(this.passwordEncoder.encode(newPassword));
        this.employeeRepository.save(employee);
    }
}
