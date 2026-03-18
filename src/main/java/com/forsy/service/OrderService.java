package com.forsy.service;

import com.forsy.dto.CreateOrderRequestDTO;
import com.forsy.dto.OrderDisplayDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Page<OrderDisplayDTO> getOrdersByClient(String clientEmail, Pageable pageable, String keyword);

    Page<OrderDisplayDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable, String keyword);

    Page<OrderDisplayDTO> getAllOrders(Pageable pageable, String keyword);

    OrderDisplayDTO addOrder(CreateOrderRequestDTO order);

    void confirmOrder(Long orderId, String employeeEmail);
    void cancelOrder(Long orderId, String employeeEmail);
}
