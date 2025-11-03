package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.CreateOrderRequestDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;
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
    @Transactional
    public OrderDTO addOrder(CreateOrderRequestDTO dto) {
        log.info("Attempting to add new order for client: {}", dto.getClientEmail());
        Employee employee = employeeRepository.findByEmail(dto.getEmployeeEmail()).orElseThrow(
                () -> new NotFoundException(String.format("Employee with email %s not found", dto.getEmployeeEmail()))
        );
        Client client = clientRepository.findByEmail(dto.getClientEmail()).orElseThrow(
                () -> new NotFoundException(String.format("Client with email %s not found", dto.getClientEmail())));
        BigDecimal totalCost = BigDecimal.ZERO;
        Order order = Order.builder()
                .employee(employee)
                .client(client)
                .orderDate(dto.getOrderDate())
                .price(totalCost)
                .build();
        List<BookItem> bookItems = new ArrayList<>();
        for (BookItemDTO itemDto : dto.getBookItems()) {
            Book book = bookRepository.findByName(itemDto.getBookName()).orElseThrow(
                    () -> new NotFoundException(String.format("Book with name %s not found", itemDto.getBookName())));
            BookItem bookItem = new BookItem();
            bookItem.setBook(book);
            bookItem.setQuantity(itemDto.getQuantity());
            bookItem.setOrder(order);
            bookItems.add(bookItem);
            totalCost = totalCost.add(book.getPrice().multiply(BigDecimal.valueOf(bookItem.getQuantity())));
        }
        if (client.getBalance().compareTo(totalCost) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds for client with email %s", dto.getClientEmail()));
        }
        client.setBalance(client.getBalance().subtract(totalCost));
        clientRepository.save(client);
        log.info("Charged client {} an amount of {}", client.getEmail(), totalCost);
        order.setPrice(totalCost);
        order.setBookItems(bookItems);
        order = orderRepository.save(order);
        log.info("Order {} created successfully", order.getId());
        return mapper.map(order, OrderDTO.class);
    }
}
