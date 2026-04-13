package com.forsy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

  @InjectMocks
  private EmployeeServiceImpl employeeService;

  @Mock
  private EmployeeRepository employeeRepository;

  @Mock
  private ClientRepository clientRepository;

  @Mock
  private ModelMapper mapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MessageSource messageSource;

  @Test
  void testGetAllEmployeesShouldReturnPagedEmployees() {
    Employee employee = Employee.builder().build();
    EmployeeDisplayDto expectedDto = new EmployeeDisplayDto();
    Pageable pageable = PageRequest.of(0, 10);
    Page<Employee> employeePage = new PageImpl<>(Collections.singletonList(employee), pageable, 1);

    when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
    when(mapper.map(employee, EmployeeDisplayDto.class)).thenReturn(expectedDto);

    Page<EmployeeDisplayDto> actualEmployeeDto = employeeService.getAllEmployees(pageable);

    verify(employeeRepository, times(1)).findAll(pageable);
    verify(mapper, times(1)).map(employee, EmployeeDisplayDto.class);

    assertEquals(1, actualEmployeeDto.getTotalElements());
    assertEquals(1, actualEmployeeDto.getContent().size());
    assertEquals(expectedDto, actualEmployeeDto.getContent().get(0));
  }

  @Nested
  class FindByEmail {

    @Test
    void testGetEmployeeByEmailShouldReturnEmployee() {
      String email = "test@test.com";
      Employee employee = Employee.builder().email(email).build();
      EmployeeDisplayDto expectedDto = EmployeeDisplayDto.builder().email(email).build();

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
      when(mapper.map(employee, EmployeeDisplayDto.class)).thenReturn(expectedDto);

      EmployeeDisplayDto actualEmployeeDto = employeeService.getEmployeeByEmail(email);

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(mapper, times(1)).map(employee, EmployeeDisplayDto.class);

      assertEquals(expectedDto, actualEmployeeDto);
    }

    @Test
    void testGetEmployeeByEmailShouldThrowExceptionWhenEmployeeNotFound() {
      String email = "test@test.com";
      String message = "Employee with email: " + email + " not found";

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq("error.user.not.found"), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> employeeService.getEmployeeByEmail(email));

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(mapper, never()).map(any(Employee.class), any());
    }
  }

  @Nested
  class UpdateByEmail {

    @Test
    void testUpdateEmployeeByEmailShouldReturnEmployeeWhenEmailStaySame() {
      String email = "test@test.com";
      String oldName = "oldName";
      String newName = "newName";
      LocalDate birthDate = LocalDate.now().minusYears(18);
      Employee employee = Employee.builder().email(email).name(oldName).build();
      EmployeeUpdateDto dto = EmployeeUpdateDto.builder().name(newName)
          .birthDate(birthDate).build();
      EmployeeDisplayDto expectedDto = EmployeeDisplayDto.builder().email(email).name(newName)
          .birthDate(birthDate).build();

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
      doNothing().when(mapper).map(dto, employee);
      when(employeeRepository.save(employee)).thenReturn(employee);
      when(mapper.map(employee, EmployeeDisplayDto.class)).thenReturn(expectedDto);

      final EmployeeDisplayDto actualEmployeeDto =
          employeeService.updateEmployeeByEmail(email, dto);

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(mapper, times(1)).map(dto, employee);
      verify(employeeRepository, times(1)).save(employee);
      verify(mapper, times(1)).map(employee, EmployeeDisplayDto.class);

      assertEquals(expectedDto, actualEmployeeDto);
    }

    @Test
    void testUpdateEmployeeByEmailShouldThrowExceptionWhenEmployeeNotFound() {
      String email = "test@test.com";
      EmployeeUpdateDto dto = EmployeeUpdateDto.builder().build();
      String message = "Employee with email: " + email + " not found";

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq("error.user.not.found"), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class,
                   () -> employeeService.updateEmployeeByEmail(email, dto));

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(mapper, never()).map(any(EmployeeDto.class), any(Employee.class));
      verify(employeeRepository, never()).save(any(Employee.class));
      verify(mapper, never()).map(any(Employee.class), any());
    }

    @Test
    void testUpdateEmployeeByEmailShouldThrowExceptionWhenBirtDateInvalid() {
      String email = "test@test.com";
      String oldName = "oldName";
      String newName = "newName";
      LocalDate birthDate = LocalDate.now().minusYears(17);
      Employee employee = Employee.builder().email(email).name(oldName).build();
      EmployeeUpdateDto dto = EmployeeUpdateDto.builder().name(newName)
          .birthDate(birthDate).build();
      String message = "Employee must be at least 18 years old";

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
      when(messageSource.getMessage(eq("error.user.underage"), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(AgeRestrictionException.class,
                   () -> employeeService.updateEmployeeByEmail(email, dto));

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(mapper, never()).map(any(EmployeeDto.class), any(Employee.class));
      verify(employeeRepository, never()).save(any(Employee.class));
      verify(mapper, never()).map(any(Employee.class), any());
    }
  }

  @Nested
  class DeleteByEmail {

    @Test
    void testDeleteEmployeeByEmailShouldReturnNothing() {
      String email = "test@test.com";
      Employee employee = Employee.builder().email(email).build();

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
      doNothing().when(employeeRepository).delete(employee);

      employeeService.deleteEmployeeByEmail(email);

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(employeeRepository, times(1)).delete(employee);
    }

    @Test
    void testDeleteEmployeeByEmailShouldReturnThrowExceptionWhenEmployeeNotFound() {
      String email = "test@test.com";
      Employee employee = Employee.builder().email(email).build();
      String message = "Employee with email: " + email + " not found";

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq("error.user.not.found"), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> employeeService.deleteEmployeeByEmail(email));

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(employeeRepository, never()).delete(any(Employee.class));
    }
  }

  @Nested
  class AddEmployee {

    @Test
    void testAddEmployeeShouldReturnEmployee() {
      String email = "test@test.com";
      String password = "password";
      LocalDate birthDate = LocalDate.now().minusYears(18);
      Employee employee = Employee.builder().email(email).birthDate(birthDate)
          .password(password).build();
      EmployeeDto dto = EmployeeDto.builder().email(email).birthDate(birthDate)
          .password(password).build();
      EmployeeDisplayDto expectedDto = EmployeeDisplayDto.builder().email(email)
          .birthDate(birthDate).build();

      when(employeeRepository.existsByEmail(email)).thenReturn(false);
      when(clientRepository.existsByEmail(email)).thenReturn(false);
      when(mapper.map(dto, Employee.class)).thenReturn(employee);
      when(passwordEncoder.encode(password)).thenReturn(password);
      when(employeeRepository.save(employee)).thenReturn(employee);
      when(mapper.map(employee, EmployeeDisplayDto.class)).thenReturn(expectedDto);

      final EmployeeDisplayDto actualEmployeeDto = employeeService.addEmployee(dto);

      verify(employeeRepository, times(1)).existsByEmail(email);
      verify(clientRepository, times(1)).existsByEmail(email);
      verify(mapper, times(1)).map(dto, Employee.class);
      verify(passwordEncoder, times(1)).encode(password);
      verify(employeeRepository, times(1)).save(employee);
      verify(mapper, times(1)).map(employee, EmployeeDisplayDto.class);

      assertEquals(expectedDto, actualEmployeeDto);
    }

    @Test
    void testAddEmployeeShouldThrowExceptionWhenEmployeeEmailAlreadyExist() {
      String email = "test@test.com";
      EmployeeDto dto = EmployeeDto.builder().email(email).build();
      String message = "Employee with email: " + email + " already exist";

      when(employeeRepository.existsByEmail(email)).thenReturn(true);
      when(messageSource.getMessage(eq("error.user.already.exist"), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(AlreadyExistException.class, () -> employeeService.addEmployee(dto));

      verify(employeeRepository, times(1)).existsByEmail(email);
      verify(clientRepository, never()).existsByEmail(anyString());
      verify(mapper, never()).map(any(EmployeeDto.class), any());
      verify(passwordEncoder, never()).encode(any(String.class));
      verify(employeeRepository, never()).save(any(Employee.class));
      verify(mapper, never()).map(any(Employee.class), any());
    }

    @Test
    void testAddEmployeeShouldThrowExceptionWhenClientEmailAlreadyExist() {
      String email = "test@test.com";
      final EmployeeDto dto = EmployeeDto.builder().email(email).build();
      ;
      String message = "Client with email: " + email + " already exist";

      when(employeeRepository.existsByEmail(email)).thenReturn(false);
      when(clientRepository.existsByEmail(email)).thenReturn(true);
      when(messageSource.getMessage(eq("error.user.already.exist"), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(AlreadyExistException.class, () -> employeeService.addEmployee(dto));

      verify(employeeRepository, times(1)).existsByEmail(email);
      verify(clientRepository, times(1)).existsByEmail(email);
      verify(mapper, never()).map(any(EmployeeDto.class), any());
      verify(passwordEncoder, never()).encode(any(String.class));
      verify(employeeRepository, never()).save(any(Employee.class));
      verify(mapper, never()).map(any(Employee.class), any());
    }

    @Test
    void testAddEmployeeShouldThrowExceptionWhenBirtDateInvalid() {
      String email = "test@test.com";
      LocalDate birthDate = LocalDate.now().minusYears(17);
      EmployeeDto dto = EmployeeDto.builder().email(email).birthDate(birthDate).build();
      String message = "Employee must be at least 18 years old";

      when(employeeRepository.existsByEmail(email)).thenReturn(false);
      when(messageSource.getMessage(eq("error.user.underage"), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(AgeRestrictionException.class, () -> employeeService.addEmployee(dto));

      verify(employeeRepository, times(1)).existsByEmail(email);
      verify(mapper, never()).map(any(EmployeeDto.class), any());
      verify(passwordEncoder, never()).encode(any(String.class));
      verify(employeeRepository, never()).save(any(Employee.class));
      verify(mapper, never()).map(any(Employee.class), any());
    }
  }

  @Nested
  class ChangePassword {

    @Test
    void testChangePasswordShouldReturn() {
      String email = "test@test.com";
      String oldPassword = "oldPassword";
      String newPassword = "newPassword";
      final ChangePasswordDto dto = ChangePasswordDto.builder().oldPassword(oldPassword)
          .newPassword(newPassword).build();
      Employee employee = Employee.builder().email(email).password(oldPassword).build();

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
      when(passwordEncoder.matches(oldPassword, employee.getPassword())).thenReturn(true);
      when(passwordEncoder.encode(newPassword)).thenReturn(newPassword);
      when(employeeRepository.save(employee)).thenReturn(employee);

      employeeService.changePassword(email, dto);

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, times(1)).matches(oldPassword, oldPassword);
      verify(passwordEncoder, times(1)).encode(newPassword);
      verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void testChangePasswordShouldThrowExceptionWhenEmailNotFound() {
      String email = "test@test.com";
      ChangePasswordDto dto = ChangePasswordDto.builder().build();
      String message = "Employee with email: " + email + " not found";

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq("error.user.not.found"), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> employeeService.changePassword(email, dto));

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
      verify(passwordEncoder, never()).encode(any(String.class));
      verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testChangePasswordShouldThrowExceptionWhenOldPasswordInvalid() {
      String email = "test@test.com";
      String employeePassword = "a";
      String dtoPassword = "b";
      final ChangePasswordDto dto = ChangePasswordDto.builder().oldPassword(dtoPassword).build();
      Employee employee = Employee.builder().email(email).password(employeePassword).build();
      String message = "Invalid password";

      when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
      when(passwordEncoder.matches(dtoPassword, employee.getPassword())).thenReturn(false);
      when(messageSource.getMessage(
          eq("error.user.old.password.not.match"), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(InvalidPasswordException.class,
                   () -> employeeService.changePassword(email, dto));

      verify(employeeRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, times(1)).matches(dtoPassword, employee.getPassword());
      verify(passwordEncoder, never()).encode(any(String.class));
      verify(employeeRepository, never()).save(any(Employee.class));
    }
  }
}
