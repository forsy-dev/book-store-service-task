package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
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
public class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    void testGetAllOrdersByClient_ShouldReturnPagedOrders() {
        String clientEmail = "test@test.com";
        Order order = Order.builder().build();
        OrderDTO expectedDto = new OrderDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(order), pageable, 1);

        when(orderRepository.findAllByClientEmail(clientEmail, pageable)).thenReturn(orderPage);
        when(mapper.map(order, OrderDTO.class)).thenReturn(expectedDto);

        Page<OrderDTO> actualOrderDto = orderService.getOrdersByClient(clientEmail, pageable);

        verify(orderRepository, times(1)).findAllByClientEmail(clientEmail, pageable);
        verify(mapper, times(1)).map(order, OrderDTO.class);

        assertEquals(1, actualOrderDto.getTotalElements());
        assertEquals(1, actualOrderDto.getContent().size());
        assertEquals(expectedDto, actualOrderDto.getContent().get(0));
    }

    @Test
    void testGetAllOrdersByEmployee_ShouldReturnPagedOrders() {
        String employeeEmail = "test@test.com";
        Order order = Order.builder().build();
        OrderDTO expectedDto = new OrderDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(order), pageable, 1);

        when(orderRepository.findAllByEmployeeEmail(employeeEmail, pageable)).thenReturn(orderPage);
        when(mapper.map(order, OrderDTO.class)).thenReturn(expectedDto);

        Page<OrderDTO> actualOrderDto = orderService.getOrdersByEmployee(employeeEmail, pageable);

        verify(orderRepository, times(1)).findAllByEmployeeEmail(employeeEmail, pageable);
        verify(mapper, times(1)).map(order, OrderDTO.class);

        assertEquals(1, actualOrderDto.getTotalElements());
        assertEquals(1, actualOrderDto.getContent().size());
        assertEquals(expectedDto, actualOrderDto.getContent().get(0));
    }
}
