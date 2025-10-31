package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.CreateOrderRequestDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper mapper;

    @Override
    public Page<OrderDTO> getOrdersByClient(String clientEmail, Pageable pageable) {
        return orderRepository.findAllByClientEmail(clientEmail, pageable).map(order -> mapper.map(order, OrderDTO.class));
    }

    @Override
    public Page<OrderDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable) {
        return orderRepository.findAllByEmployeeEmail(employeeEmail, pageable).map(order -> mapper.map(order, OrderDTO.class));
    }

    @Override
    public OrderDTO addOrder(CreateOrderRequestDTO order) {
        return null;
    }
}
