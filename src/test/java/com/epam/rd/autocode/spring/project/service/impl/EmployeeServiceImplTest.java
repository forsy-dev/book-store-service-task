package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.AgeRestrictionException;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.InvalidPasswordException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

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
    void testGetAllEmployees_ShouldReturnPagedEmployees() {
        Employee employee = Employee.builder().build();
        EmployeeDisplayDTO expectedDto = new EmployeeDisplayDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Collections.singletonList(employee), pageable, 1);

        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);
        when(mapper.map(employee, EmployeeDisplayDTO.class)).thenReturn(expectedDto);

        Page<EmployeeDisplayDTO> actualEmployeeDto = employeeService.getAllEmployees(pageable);

        verify(employeeRepository, times(1)).findAll(pageable);
        verify(mapper, times(1)).map(employee, EmployeeDisplayDTO.class);

        assertEquals(1, actualEmployeeDto.getTotalElements());
        assertEquals(1, actualEmployeeDto.getContent().size());
        assertEquals(expectedDto, actualEmployeeDto.getContent().get(0));
    }

    @Nested
    class FindByEmail {

        @Test
        void testGetEmployeeByEmail_ShouldReturnEmployee() {
            String email = "test@test.com";
            Employee employee = Employee.builder().email(email).build();
            EmployeeDisplayDTO expectedDto = EmployeeDisplayDTO.builder().email(email).build();

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
            when(mapper.map(employee, EmployeeDisplayDTO.class)).thenReturn(expectedDto);

            EmployeeDisplayDTO actualEmployeeDto = employeeService.getEmployeeByEmail(email);

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(mapper, times(1)).map(employee, EmployeeDisplayDTO.class);

            assertEquals(expectedDto, actualEmployeeDto);
        }

        @Test
        void testGetEmployeeByEmail_ShouldThrowExceptionWhenEmployeeNotFound() {
            String email = "test@test.com";
            String message = "Employee with email: " + email + " not found";

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq("error.user.not.found"), any(), any(Locale.class))).thenReturn(message);

            assertThrows(NotFoundException.class, () -> employeeService.getEmployeeByEmail(email));

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(mapper, never()).map(any(Employee.class), any());
        }
    }

    @Nested
    class UpdateByEmail {

        @Test
        void testUpdateEmployeeByEmail_ShouldReturnEmployeeWhenEmailStaySame() {
            String email = "test@test.com";
            String oldName = "oldName";
            String newName = "newName";
            LocalDate birthDate = LocalDate.now().minusYears(18);
            Employee employee = Employee.builder().email(email).name(oldName).build();
            EmployeeUpdateDTO dto = EmployeeUpdateDTO.builder().name(newName).birthDate(birthDate).build();
            EmployeeDisplayDTO expectedDto = EmployeeDisplayDTO.builder().email(email).name(newName).birthDate(birthDate).build();

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
            doNothing().when(mapper).map(dto, employee);
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(mapper.map(employee, EmployeeDisplayDTO.class)).thenReturn(expectedDto);

            EmployeeDisplayDTO actualEmployeeDto = employeeService.updateEmployeeByEmail(email, dto);

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(mapper, times(1)).map(dto, employee);
            verify(employeeRepository, times(1)).save(employee);
            verify(mapper, times(1)).map(employee, EmployeeDisplayDTO.class);

            assertEquals(expectedDto, actualEmployeeDto);
        }

        @Test
        void testUpdateEmployeeByEmail_ShouldThrowExceptionWhenEmployeeNotFound() {
            String email = "test@test.com";
            EmployeeUpdateDTO dto = EmployeeUpdateDTO.builder().build();
            String message = "Employee with email: " + email + " not found";

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq("error.user.not.found"), any(), any(Locale.class))).thenReturn(message);

            assertThrows(NotFoundException.class, () -> employeeService.updateEmployeeByEmail(email, dto));

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(mapper, never()).map(any(EmployeeDTO.class), any(Employee.class));
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(mapper, never()).map(any(Employee.class), any());
        }

        @Test
        void testUpdateEmployeeByEmail_ShouldThrowExceptionWhenBirtDateInvalid() {
            String email = "test@test.com";
            String oldName = "oldName";
            String newName = "newName";
            LocalDate birthDate = LocalDate.now().minusYears(17);
            Employee employee = Employee.builder().email(email).name(oldName).build();
            EmployeeUpdateDTO dto = EmployeeUpdateDTO.builder().name(newName).birthDate(birthDate).build();
            String message = "Employee must be at least 18 years old";

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
            when(messageSource.getMessage(eq("error.user.underage"), any(), any(Locale.class))).thenReturn(message);

            assertThrows(AgeRestrictionException.class, () -> employeeService.updateEmployeeByEmail(email, dto));

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(mapper, never()).map(any(EmployeeDTO.class), any(Employee.class));
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(mapper, never()).map(any(Employee.class), any());
        }
    }

    @Nested
    class DeleteByEmail {

        @Test
        void testDeleteEmployeeByEmail_ShouldReturnNothing() {
            String email = "test@test.com";
            Employee employee = Employee.builder().email(email).build();

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            employeeService.deleteEmployeeByEmail(email);

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(employeeRepository, times(1)).delete(employee);
        }

        @Test
        void testDeleteEmployeeByEmail_ShouldReturnThrowExceptionWhenEmployeeNotFound() {
            String email = "test@test.com";
            Employee employee = Employee.builder().email(email).build();
            String message = "Employee with email: " + email + " not found";

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq("error.user.not.found"), any(), any(Locale.class))).thenReturn(message);

            assertThrows(NotFoundException.class, () -> employeeService.deleteEmployeeByEmail(email));

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(employeeRepository, never()).delete(any(Employee.class));
        }
    }

    @Nested
    class AddEmployee {

        @Test
        void testAddEmployee_ShouldReturnEmployee() {
            String email = "test@test.com";
            String password = "password";
            LocalDate birthDate = LocalDate.now().minusYears(18);
            Employee employee = Employee.builder().email(email).birthDate(birthDate).password(password).build();
            EmployeeDTO dto = EmployeeDTO.builder().email(email).birthDate(birthDate).password(password).build();
            EmployeeDisplayDTO expectedDto = EmployeeDisplayDTO.builder().email(email).birthDate(birthDate).build();

            when(employeeRepository.existsByEmail(email)).thenReturn(false);
            when(clientRepository.existsByEmail(email)).thenReturn(false);
            when(mapper.map(dto, Employee.class)).thenReturn(employee);
            when(passwordEncoder.encode(password)).thenReturn(password);
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(mapper.map(employee, EmployeeDisplayDTO.class)).thenReturn(expectedDto);

            EmployeeDisplayDTO actualEmployeeDto = employeeService.addEmployee(dto);

            verify(employeeRepository, times(1)).existsByEmail(email);
            verify(clientRepository, times(1)).existsByEmail(email);
            verify(mapper, times(1)).map(dto, Employee.class);
            verify(passwordEncoder, times(1)).encode(password);
            verify(employeeRepository, times(1)).save(employee);
            verify(mapper, times(1)).map(employee, EmployeeDisplayDTO.class);

            assertEquals(expectedDto, actualEmployeeDto);
        }

        @Test
        void testAddEmployee_ShouldThrowExceptionWhenEmployeeEmailAlreadyExist() {
            String email = "test@test.com";
            EmployeeDTO dto = EmployeeDTO.builder().email(email).build();
            String message = "Employee with email: " + email + " already exist";

            when(employeeRepository.existsByEmail(email)).thenReturn(true);
            when(messageSource.getMessage(eq("error.user.already.exist"), any(), any(Locale.class))).thenReturn(message);

            assertThrows(AlreadyExistException.class, () -> employeeService.addEmployee(dto));

            verify(employeeRepository, times(1)).existsByEmail(email);
            verify(clientRepository, never()).existsByEmail(anyString());
            verify(mapper, never()).map(any(EmployeeDTO.class), any());
            verify(passwordEncoder, never()).encode(any(String.class));
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(mapper, never()).map(any(Employee.class), any());
        }

        @Test
        void testAddEmployee_ShouldThrowExceptionWhenClientEmailAlreadyExist() {
            String email = "test@test.com";
            EmployeeDTO dto = EmployeeDTO.builder().email(email).build();;
            String message = "Client with email: " + email + " already exist";

            when(employeeRepository.existsByEmail(email)).thenReturn(false);
            when(clientRepository.existsByEmail(email)).thenReturn(true);
            when(messageSource.getMessage(eq("error.user.already.exist"), any(), any(Locale.class))).thenReturn(message);

            assertThrows(AlreadyExistException.class, () -> employeeService.addEmployee(dto));

            verify(employeeRepository, times(1)).existsByEmail(email);
            verify(clientRepository, times(1)).existsByEmail(email);
            verify(mapper, never()).map(any(EmployeeDTO.class), any());
            verify(passwordEncoder, never()).encode(any(String.class));
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(mapper, never()).map(any(Employee.class), any());
        }

        @Test
        void testAddEmployee_ShouldThrowExceptionWhenBirtDateInvalid() {
            String email = "test@test.com";
            LocalDate birthDate = LocalDate.now().minusYears(17);
            EmployeeDTO dto = EmployeeDTO.builder().email(email).birthDate(birthDate).build();
            String message = "Employee must be at least 18 years old";

            when(employeeRepository.existsByEmail(email)).thenReturn(false);
            when(messageSource.getMessage(eq("error.user.underage"), any(), any(Locale.class))).thenReturn(message);

            assertThrows(AgeRestrictionException.class, () -> employeeService.addEmployee(dto));

            verify(employeeRepository, times(1)).existsByEmail(email);
            verify(mapper, never()).map(any(EmployeeDTO.class), any());
            verify(passwordEncoder, never()).encode(any(String.class));
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(mapper, never()).map(any(Employee.class), any());
        }
    }

    @Nested
    class ChangePassword {

        @Test
        void testChangePassword_ShouldReturn() {
            String email = "test@test.com";
            String oldPassword = "oldPassword";
            String newPassword = "newPassword";
            ChangePasswordDTO dto = ChangePasswordDTO.builder().oldPassword(oldPassword).newPassword(newPassword).build();
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
        void testChangePassword_ShouldThrowExceptionWhenEmailNotFound() {
            String email = "test@test.com";
            ChangePasswordDTO dto = ChangePasswordDTO.builder().build();
            String message = "Employee with email: " + email + " not found";

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(messageSource.getMessage(eq("error.user.not.found"), any(), any(Locale.class))).thenReturn(message);

            assertThrows(NotFoundException.class, () -> employeeService.changePassword(email, dto));

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
            verify(passwordEncoder, never()).encode(any(String.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        void testChangePassword_ShouldThrowExceptionWhenOldPasswordInvalid() {
            String email = "test@test.com";
            String employeePassword = "a";
            String dtoPassword = "b";
            ChangePasswordDTO dto = ChangePasswordDTO.builder().oldPassword(dtoPassword).build();
            Employee employee = Employee.builder().email(email).password(employeePassword).build();
            String message = "Invalid password";

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
            when(passwordEncoder.matches(dtoPassword, employee.getPassword())).thenReturn(false);
            when(messageSource.getMessage(eq("error.user.old.password.not.match"), any(), any(Locale.class))).thenReturn(message);

            assertThrows(InvalidPasswordException.class, () -> employeeService.changePassword(email, dto));

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(passwordEncoder, times(1)).matches(dtoPassword, employee.getPassword());
            verify(passwordEncoder, never()).encode(any(String.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }
}
