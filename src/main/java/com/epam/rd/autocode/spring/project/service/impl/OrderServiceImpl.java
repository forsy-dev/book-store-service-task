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
    public Page<OrderDisplayDTO> getOrdersByClient(String clientEmail, Pageable pageable, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return orderRepository.searchByClient(clientEmail, keyword, pageable).map(this::mapToDisplayDTO);
        }
        return orderRepository.findAllByClientEmail(clientEmail, pageable).map(this::mapToDisplayDTO);
    }

    @Override
    public Page<OrderDisplayDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return orderRepository.searchByEmployee(employeeEmail, keyword, pageable).map(this::mapToDisplayDTO);
        }
        return orderRepository.findAllByEmployeeEmail(employeeEmail, pageable).map(this::mapToDisplayDTO);
    }

    @Override
    public Page<OrderDisplayDTO> getAllOrders(Pageable pageable, String keyword) {
        Page<Order> orders;
        if (keyword != null && !keyword.trim().isEmpty()) {
            orders = orderRepository.searchOrders(keyword, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::mapToDisplayDTO);
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
    @Transactional
    public void confirmOrder(Long orderId, String employeeEmail) {
        log.info("Attempting to confirm order with id {}", orderId);

        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NotFoundException("Order with id " + orderId + " not found"));
        Employee employee = employeeRepository.findByEmail(employeeEmail).orElseThrow(
                () -> new NotFoundException(String.format("Employee with email %s not found", employeeEmail)));
        OrderStatusRecord orderStatusRecord = orderStatusRepository.findByOrderId(orderId).orElseThrow(
                () -> new NotFoundException("Order status record for order with id " + orderId + " not found"));

        if (orderStatusRecord.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order status is not PENDING");
        }

        Client client = order.getClient();
        if (client.getBalance().compareTo(order.getPrice()) < 0) {
            throw new InsufficientFundsException("Client does not have enough funds to confirm this order.");
        }

        order.setEmployee(employee);
        orderRepository.save(order);
        orderStatusRecord.setStatus(OrderStatus.CONFIRMED);
        orderStatusRepository.save(orderStatusRecord);
        client.setBalance(client.getBalance().subtract(order.getPrice()));
        clientRepository.save(client);
        log.info("Order {} confirmed successfully", orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String employeeEmail) {
        log.info("Attempting to cancel order with id {}", orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NotFoundException("Order with id " + orderId + " not found"));
        Employee employee = employeeRepository.findByEmail(employeeEmail).orElseThrow(
                () -> new NotFoundException(String.format("Employee with email %s not found", employeeEmail)));
        OrderStatusRecord orderStatusRecord = orderStatusRepository.findByOrderId(orderId).orElseThrow(
                () -> new NotFoundException("Order status record for order with id " + orderId + " not found"));
        if (orderStatusRecord.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order status is not PENDING");
        }
        orderStatusRecord.setStatus(OrderStatus.CANCELED);
        orderStatusRepository.save(orderStatusRecord);
        order.setEmployee(employee);
        orderRepository.save(order);
        log.info("Order {} canceled successfully", orderId);
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
