package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ChangePasswordDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.InvalidPasswordException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;

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
    public EmployeeDisplayDTO updateEmployeeByEmail(String email, EmployeeUpdateDTO dto) {
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
        log.info("Attempting to delete employee with email {}", email);
        employeeRepository.findByEmail(email).ifPresentOrElse(employee -> {
                    employeeRepository.delete(employee);
                    log.info("Employee with email {} deleted successfully", email);
                },
                () -> {
            throw new NotFoundException(String.format("Employee with email %s not found", email));
        });
    }

    @Override
    public EmployeeDisplayDTO addEmployee(EmployeeDTO employee) {
        log.info("Attempting to add employee with email {}", employee.getEmail());
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new AlreadyExistException(String.format("Employee with email %s already exists", employee.getEmail()));
        }
        validateAge(employee.getBirthDate());
        Employee newEmployee = mapper.map(employee, Employee.class);
        newEmployee.setPassword(passwordEncoder.encode(employee.getPassword()));
        newEmployee = employeeRepository.save(newEmployee);
        log.info("Employee with email {} added successfully", newEmployee.getEmail());
        return mapper.map(newEmployee, EmployeeDisplayDTO.class);
    }

    @Override
    public void changePassword(String email, ChangePasswordDTO dto) {
        log.info("Attempting to change password for employee with email {}", email);
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(String.format("Employee with email %s not found", email)));
        if (!passwordEncoder.matches(dto.getOldPassword(), employee.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }
        employee.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        employeeRepository.save(employee);
        log.info("Password for employee with email {} changed successfully", email);
    }
}
