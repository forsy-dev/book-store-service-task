package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDisplayDTO;
import com.epam.rd.autocode.spring.project.exception.AgeRestrictionException;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
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
    private ModelMapper mapper;

    @Test
    void testGetAllEmployees_ShouldReturnPagedEmployees() {
        Employee employee = Employee.builder().build();
        EmployeeDisplayDTO expectedDto = new EmployeeDisplayDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(employee), pageable, 1);

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

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());

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
            EmployeeDTO dto = EmployeeDTO.builder().email(email).name(newName).birthDate(birthDate).build();
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
            EmployeeDTO dto = EmployeeDTO.builder().build();

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> employeeService.updateEmployeeByEmail(email, dto));

            verify(employeeRepository, times(1)).findByEmail(email);
            verify(mapper, never()).map(any(EmployeeDTO.class), any(Employee.class));
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(mapper, never()).map(any(Employee.class), any());
        }

        @Test
        void testUpdateEmployeeByEmail_ShouldThrowExceptionWhenEmailAlreadyExist() {
            String oldEmail = "test@test.com";
            String newEmail = "test@test.org";
            Employee employee = Employee.builder().email(oldEmail).build();
            EmployeeDTO dto = EmployeeDTO.builder().email(newEmail).build();

            when(employeeRepository.findByEmail(oldEmail)).thenReturn(Optional.of(employee));
            when(employeeRepository.existsByEmail(newEmail)).thenReturn(true);

            assertThrows(AlreadyExistException.class, () -> employeeService.updateEmployeeByEmail(oldEmail, dto));

            verify(employeeRepository, times(1)).findByEmail(oldEmail);
            verify(employeeRepository, times(1)).existsByEmail(newEmail);
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
            EmployeeDTO dto = EmployeeDTO.builder().email(email).name(newName).birthDate(birthDate).build();

            when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));

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

            when(employeeRepository.existsByEmail(email)).thenReturn(true);
            doNothing().when(employeeRepository).deleteByEmail(email);

            employeeService.deleteEmployeeByEmail(email);

            verify(employeeRepository, times(1)).existsByEmail(email);
            verify(employeeRepository, times(1)).deleteByEmail(email);
        }

        @Test
        void testDeleteEmployeeByEmail_ShouldThrowExceptionWhenEmployeeNotFound() {
            String email = "test@test.com";

            when(employeeRepository.existsByEmail(email)).thenReturn(false);

            assertThrows(NotFoundException.class, () -> employeeService.deleteEmployeeByEmail(email));

            verify(employeeRepository, times(1)).existsByEmail(email);
            verify(employeeRepository, never()).deleteByEmail(any(String.class));
        }
    }
}
