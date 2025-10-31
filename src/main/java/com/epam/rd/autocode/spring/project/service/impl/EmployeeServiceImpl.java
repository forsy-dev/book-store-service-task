package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDisplayDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.AgeRestrictionException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper mapper;

    @Override
    public Page<EmployeeDisplayDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employee -> mapper.map(employee, EmployeeDisplayDTO.class));
    }

    @Override
    public EmployeeDisplayDTO getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email).map(employee -> mapper.map(employee, EmployeeDisplayDTO.class))
                .orElseThrow(() -> new NotFoundException(String.format("Employee with email %s not found", email)));
    }

    @Override
    public EmployeeDisplayDTO updateEmployeeByEmail(String email, EmployeeDTO dto) {
        log.info("Attempting to update employee with old email: {}", email);
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(String.format("Employee with email %s not found", email)));
        if (!email.equals(dto.getEmail()) && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new AlreadyExistException(String.format("Employee with email %s already exists", dto.getEmail()));
        }
        validateAge(dto.getBirthDate());
        mapper.map(dto, employee);
        employee = employeeRepository.save(employee);
        log.info("Employee with email {} updated successfully", employee.getEmail());
        return mapper.map(employee, EmployeeDisplayDTO.class);
    }

    private void validateAge(LocalDate birthDate) {
        LocalDate minimumValidDate = LocalDate.now().minusYears(18);
        if (birthDate.isAfter(minimumValidDate)) {
            throw new AgeRestrictionException("Employee must be at least 18 years old to work.");
        }
    }

    @Override
    public void deleteEmployeeByEmail(String email) {

    }

    @Override
    public EmployeeDisplayDTO addEmployee(EmployeeDTO employee) {
        return null;
    }
}
