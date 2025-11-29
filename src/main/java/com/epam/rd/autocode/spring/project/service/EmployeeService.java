package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ChangePasswordDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    Page<EmployeeDisplayDTO> getAllEmployees(Pageable pageable);

    EmployeeDisplayDTO getEmployeeByEmail(String email);

    EmployeeDisplayDTO updateEmployeeByEmail(String email, EmployeeUpdateDTO dto);

    void deleteEmployeeByEmail(String email);

    EmployeeDisplayDTO addEmployee(EmployeeDTO employee);

    void changePassword(String email, ChangePasswordDTO dto);
}
