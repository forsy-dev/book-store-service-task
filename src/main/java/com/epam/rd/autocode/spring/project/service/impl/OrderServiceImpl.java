package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.CreateOrderRequestDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDisplayDTO;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
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
    private final OrderStatusRepository orderStatusRepository;
    private final ModelMapper mapper;

    @Override
    public Page<OrderDisplayDTO> getOrdersByClient(String clientEmail, Pageable pageable) {
        return orderRepository.findAllByClientEmail(clientEmail, pageable).map(this::mapToDisplayDTO);
    }

    @Override
    public Page<OrderDisplayDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable) {
        return orderRepository.findAllByEmployeeEmail(employeeEmail, pageable).map(this::mapToDisplayDTO);
    }

    @Override
    public Page<OrderDisplayDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToDisplayDTO);
    }

    @Override
    @Transactional
    public OrderDisplayDTO addOrder(CreateOrderRequestDTO dto) {
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

        order.setPrice(totalCost);
        order.setBookItems(bookItems);
        order = orderRepository.save(order);

        OrderStatusRecord statusRecord = OrderStatusRecord.builder()
                .orderId(order.getId())
                .status(OrderStatus.PENDING)
                .build();
        orderStatusRepository.save(statusRecord);

        log.info("Order {} created successfully in PENDING state", order.getId());
        return mapToDisplayDTO(order);
    }

    @Override
    public void confirmOrder(Long orderId) {

    }

    @Override
    public void cancelOrder(Long orderId) {

    }

    private OrderDisplayDTO mapToDisplayDTO(Order order) {
        OrderDisplayDTO dto = mapper.map(order, OrderDisplayDTO.class);
        // Manually fetch status
        OrderStatus status = orderStatusRepository.findByOrderId(order.getId())
                .map(OrderStatusRecord::getStatus)
                .orElse(OrderStatus.PENDING); // Default if missing
        dto.setStatus(status);
        dto.setId(order.getId());
        return dto;
    }
}
