package com.forsy.service;

import com.forsy.dto.ChangePasswordDto;
import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.EmployeeDto;
import com.forsy.dto.EmployeeUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining the business logic for managing bookstore employees.
 *
 * <p>This interface serves as the authoritative layer for staff-related operations,
 * providing mechanisms for registration, profile management, and security
 * credential updates. It ensures administrative integrity by validating age
 * requirements and preventing identity collisions across the system.
 *
 * @author Illia
 */
public interface EmployeeService {

  /**
   * Retrieves a paginated collection of all registered employees.
   *
   * @param pageable the pagination and sorting parameters
   * @return a {@link Page} of {@link EmployeeDisplayDto} objects representing
   *     the current bookstore staff
   */
  Page<EmployeeDisplayDto> getAllEmployees(Pageable pageable);

  /**
   * Locates a specific employee using their unique professional email address.
   *
   * @param email the unique email of the employee to retrieve
   * @return the {@link EmployeeDisplayDto} representing the found employee
   * @throws com.forsy.exception.NotFoundException if no employee exists with the given email
   */
  EmployeeDisplayDto getEmployeeByEmail(String email);

  /**
   * Updates the professional profile information of an existing employee.
   *
   * <p>This operation includes age validation to ensure the employee meets
   * the minimum age requirement of 18 years.
   *
   * @param email the current email of the employee to be modified
   * @param dto   the data transfer object containing updated profile information
   * @return the updated {@link EmployeeDisplayDto}
   * @throws com.forsy.exception.NotFoundException       if the employee is not found
   * @throws com.forsy.exception.AgeRestrictionException if the birth date indicates
   *                                                     the employee is under the required age
   */
  EmployeeDisplayDto updateEmployeeByEmail(String email, EmployeeUpdateDto dto);

  /**
   * Permanently removes an employee account from the professional registry.
   *
   * @param email the unique email of the employee to be deleted
   * @throws com.forsy.exception.NotFoundException if the employee record does not exist
   */
  void deleteEmployeeByEmail(String email);

  /**
   * Registers a new employee into the bookstore staff.
   *
   * <p>Handles password encryption and verifies that the email is not
   * already in use by either an employee or a client. Validates
   * that the applicant meets the 18-year age requirement.
   *
   * @param employee the data transfer object containing registration details
   * @return the {@link EmployeeDisplayDto} of the newly created employee
   * @throws com.forsy.exception.AlreadyExistException   if the email is already registered
   *                                                     in the system
   * @throws com.forsy.exception.AgeRestrictionException if the applicant
   *                                                     is under the required age
   */
  EmployeeDisplayDto addEmployee(EmployeeDto employee);

  /**
   * Updates the security credentials for a specific employee.
   *
   * <p>Verifies the validity of the current password before encoding
   * and persisting the new credential.
   *
   * @param email the unique email of the employee
   * @param dto   the data transfer object containing current and new passwords
   * @throws com.forsy.exception.NotFoundException        if the employee is not found
   * @throws com.forsy.exception.InvalidPasswordException if the current
   *                                                      password does not match the record
   */
  void changePassword(String email, ChangePasswordDto dto);
}
