package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDisplayDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
