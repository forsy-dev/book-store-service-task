package com.forsy.service.impl;

import com.forsy.dto.ChangePasswordDto;
import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.EmployeeDto;
import com.forsy.dto.EmployeeUpdateDto;
import com.forsy.exception.AgeRestrictionException;
import com.forsy.exception.AlreadyExistException;
import com.forsy.exception.InvalidPasswordException;
import com.forsy.exception.NotFoundException;
import com.forsy.model.Employee;
import com.forsy.repo.ClientRepository;
import com.forsy.repo.EmployeeRepository;
import com.forsy.service.EmployeeService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link EmployeeService} for managing staff-related business logic.
 *
 * <p>This service manages the lifecycle of employee records, ensuring
 * strict age compliance and identity uniqueness across both employee and
 * client domains. It utilizes {@link ModelMapper} for DTO conversions
 * and {@link PasswordEncoder} for securing administrative credentials.
 *
 * @author Illia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final ClientRepository clientRepository;
  private final ModelMapper mapper;
  private final PasswordEncoder passwordEncoder;
  private final MessageSource messageSource;

  /**
   * {@inheritDoc}
   */
  @Override
  public Page<EmployeeDisplayDto> getAllEmployees(Pageable pageable) {
    return employeeRepository.findAll(pageable)
        .map(employee -> mapper.map(employee, EmployeeDisplayDto.class));
  }

  /**
   * {@inheritDoc}
   *
   * @throws NotFoundException if the requested employee email does not
   *                           exist in the repository
   */
  @Override
  public EmployeeDisplayDto getEmployeeByEmail(String email) {
    return employeeRepository.findByEmail(email)
        .map(employee -> mapper.map(employee, EmployeeDisplayDto.class))
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              "error.user.not.found", new Object[]{email},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });
  }

  /**
   * {@inheritDoc}
   *
   * <p>Validates the new birth date before applying updates to ensure
   * the employee remains compliant with age policies.
   *
   * @throws NotFoundException       if the employee to update is not found
   * @throws AgeRestrictionException if the birth date indicates the
   *                                 user is under 18 years old
   */
  @Override
  public EmployeeDisplayDto updateEmployeeByEmail(String email, EmployeeUpdateDto dto) {
    log.info("Attempting to update employee with old email: {}", email);

    Employee employee = employeeRepository.findByEmail(email).orElseThrow(() -> {
      String message = messageSource.getMessage(
          "error.user.not.found", new Object[]{email},
          LocaleContextHolder.getLocale());
      return new NotFoundException(message);
    });

    validateAge(dto.getBirthDate());
    mapper.map(dto, employee);
    employee = employeeRepository.save(employee);

    log.info("Employee with email {} updated successfully", employee.getEmail());
    return mapper.map(employee, EmployeeDisplayDto.class);
  }

  /**
   * Verifies that the provided birth date meets the minimum age requirement.
   *
   * @param birthDate the date of birth to validate
   * @throws AgeRestrictionException if the date is less than 18 years
   *                                 from the current date
   */
  private void validateAge(LocalDate birthDate) {
    LocalDate minimumValidDate = LocalDate.now().minusYears(18);
    if (birthDate.isAfter(minimumValidDate)) {
      String message = messageSource.getMessage(
          "error.user.underage", new Object[]{},
          LocaleContextHolder.getLocale());
      throw new AgeRestrictionException(message);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @throws NotFoundException if the target email is not found
   */
  @Override
  public void deleteEmployeeByEmail(String email) {
    log.info("Attempting to delete employee with email {}", email);
    employeeRepository.findByEmail(email).ifPresentOrElse(
        employee -> {
          employeeRepository.delete(employee);
          log.info("Employee with email {} deleted successfully", email);
        },
        () -> {
          String message = messageSource.getMessage(
              "error.user.not.found", new Object[]{email},
              LocaleContextHolder.getLocale());
          throw new NotFoundException(message);
        });
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks both employee and client registries for email uniqueness.
   * Encodes the initial password and validates the 18-year age policy
   * before registration.
   *
   * @throws AlreadyExistException if the email is already registered
   *                               to any user role
   */
  @Override
  public EmployeeDisplayDto addEmployee(EmployeeDto employee) {
    log.info("Attempting to add employee with email {}", employee.getEmail());
    if (employeeRepository.existsByEmail(employee.getEmail())
        || clientRepository.existsByEmail(employee.getEmail())) {
      String message = messageSource.getMessage(
          "error.user.already.exist", new Object[]{employee.getEmail()},
          LocaleContextHolder.getLocale());
      throw new AlreadyExistException(message);
    }

    validateAge(employee.getBirthDate());
    Employee newEmployee = mapper.map(employee, Employee.class);
    newEmployee.setPassword(passwordEncoder.encode(employee.getPassword()));
    newEmployee = employeeRepository.save(newEmployee);
    log.info("Employee with email {} added successfully", newEmployee.getEmail());
    return mapper.map(newEmployee, EmployeeDisplayDto.class);
  }

  /**
   * {@inheritDoc}
   *
   * @throws InvalidPasswordException if the provided old password does
   *                                  not match the persistent record
   */
  @Override
  public void changePassword(String email, ChangePasswordDto dto) {
    log.info("Attempting to change password for employee with email {}", email);
    Employee employee = employeeRepository.findByEmail(email).orElseThrow(() -> {
      String message = messageSource.getMessage(
          "error.user.not.found", new Object[]{email},
          LocaleContextHolder.getLocale());
      return new NotFoundException(message);
    });

    if (!passwordEncoder.matches(dto.getOldPassword(), employee.getPassword())) {
      String message = messageSource.getMessage(
          "error.user.old.password.not.match", new Object[]{email},
          LocaleContextHolder.getLocale());
      throw new InvalidPasswordException(message);
    }

    employee.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    employeeRepository.save(employee);
    log.info("Password for employee with email {} changed successfully", email);
  }
}
