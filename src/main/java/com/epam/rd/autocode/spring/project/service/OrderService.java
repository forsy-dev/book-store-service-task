package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.CreateOrderRequestDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDisplayDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface OrderService {

    Page<OrderDisplayDTO> getOrdersByClient(String clientEmail, Pageable pageable);

    Page<OrderDisplayDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable);

    Page<OrderDisplayDTO> getAllOrders(Pageable pageable);

    OrderDisplayDTO addOrder(CreateOrderRequestDTO order);

    void confirmOrder(Long orderId);
    void cancelOrder(Long orderId);
}
