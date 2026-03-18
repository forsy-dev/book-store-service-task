package com.forsy.service;

import com.forsy.dto.ChangePasswordDTO;
import com.forsy.dto.EmployeeDTO;
import com.forsy.dto.EmployeeDisplayDTO;
import com.forsy.dto.EmployeeUpdateDTO;
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
