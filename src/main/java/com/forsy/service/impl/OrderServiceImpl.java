package com.forsy.service.impl;

import com.forsy.dto.BookItemDto;
import com.forsy.dto.CreateOrderRequestDto;
import com.forsy.dto.OrderDisplayDto;
import com.forsy.exception.InsufficientFundsException;
import com.forsy.exception.NotFoundException;
import com.forsy.model.Book;
import com.forsy.model.BookItem;
import com.forsy.model.Client;
import com.forsy.model.Employee;
import com.forsy.model.Order;
import com.forsy.model.OrderStatusRecord;
import com.forsy.model.enums.OrderStatus;
import com.forsy.repo.BookRepository;
import com.forsy.repo.ClientRepository;
import com.forsy.repo.EmployeeRepository;
import com.forsy.repo.OrderRepository;
import com.forsy.repo.OrderStatusRepository;
import com.forsy.service.OrderService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link OrderService} for managing complex order lifecycles.
 *
 * <p>This service coordinates between clients, employees, and inventory
 * to handle order creation, status transitions, and financial settlements.
 * All state-changing operations are wrapped in transactions to maintain
 * consistency across the order and status repositories.
 *
 * @author Illia
 */
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
  private final MessageSource messageSource;
  private final CacheManager cacheManager;

  /**
   * {@inheritDoc}
   */
  @Override
  public Page<OrderDisplayDto> getOrdersByClient(
      String clientEmail, Pageable pageable, String keyword) {
    if (keyword != null && !keyword.trim().isEmpty()) {
      return orderRepository.searchByClient(clientEmail, keyword, pageable)
          .map(this::mapToDisplayDto);
    }
    return orderRepository.findAllByClientEmail(clientEmail, pageable)
        .map(this::mapToDisplayDto);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Page<OrderDisplayDto> getOrdersByEmployee(
      String employeeEmail, Pageable pageable, String keyword) {
    if (keyword != null && !keyword.trim().isEmpty()) {
      return orderRepository.searchByEmployee(employeeEmail, keyword, pageable)
          .map(this::mapToDisplayDto);
    }
    return orderRepository.findAllByEmployeeEmail(employeeEmail, pageable)
        .map(this::mapToDisplayDto);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Page<OrderDisplayDto> getAllOrders(Pageable pageable, String keyword) {
    Page<Order> orders;
    if (keyword != null && !keyword.trim().isEmpty()) {
      orders = orderRepository.searchOrders(keyword, pageable);
    } else {
      orders = orderRepository.findAll(pageable);
    }
    return orders.map(this::mapToDisplayDto);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Calculates total cost by iterating through requested book items and
   * persists the order along with an initial PENDING status record.
   *
   * @throws NotFoundException if the client, employee, or any book in the
   *                           request is not found
   */
  @Override
  @Transactional
  public OrderDisplayDto addOrder(CreateOrderRequestDto dto) {
    log.info("Attempting to add new order for client: {}", dto.getClientEmail());

    Employee employee = employeeRepository.findByEmail(dto.getEmployeeEmail())
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              "error.user.not.found", new Object[]{dto.getEmployeeEmail()},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });

    Client client = clientRepository.findByEmail(dto.getClientEmail())
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              "error.user.not.found", new Object[]{dto.getClientEmail()},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });

    BigDecimal totalCost = BigDecimal.ZERO;
    Order order = Order.builder()
        .employee(employee)
        .client(client)
        .orderDate(dto.getOrderDate())
        .price(totalCost)
        .build();

    List<BookItem> bookItems = new ArrayList<>();
    for (BookItemDto itemDto : dto.getBookItems()) {
      Book book = bookRepository.findByName(itemDto.getBookName())
          .orElseThrow(() -> {
            String message = messageSource.getMessage(
                "error.book.not.found", new Object[]{itemDto.getBookName()},
                LocaleContextHolder.getLocale());
            return new NotFoundException(message);
          });

      BookItem bookItem = new BookItem();
      bookItem.setBook(book);
      bookItem.setQuantity(itemDto.getQuantity());
      bookItem.setOrder(order);
      bookItems.add(bookItem);
      totalCost = totalCost.add(book.getPrice()
                                    .multiply(BigDecimal.valueOf(bookItem.getQuantity())));
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
    return mapToDisplayDto(order);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Validates state, verifies client balance, deducts funds, and
   * updates status to CONFIRMED within a single transaction.
   *
   * @throws NotFoundException          if the order, employee, or status record is missing
   * @throws IllegalStateException      if the order is not currently PENDING
   * @throws InsufficientFundsException if the client cannot afford the total price
   */
  @Override
  @Transactional
  public void confirmOrder(Long orderId, String employeeEmail) {
    log.info("Attempting to confirm order with id {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              "error.order.not.found", new Object[]{orderId},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });

    Employee employee = employeeRepository.findByEmail(employeeEmail)
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              "error.user.not.found", new Object[]{employeeEmail},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });

    OrderStatusRecord orderStatusRecord = orderStatusRepository.findByOrderId(orderId)
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              "error.order.status.not.found", new Object[]{orderId},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });

    if (orderStatusRecord.getStatus() != OrderStatus.PENDING) {
      String message = messageSource.getMessage(
          "error.order.status.not.pending", new Object[]{},
          LocaleContextHolder.getLocale());
      throw new IllegalStateException(message);
    }

    Client client = order.getClient();
    if (client.getBalance().compareTo(order.getPrice()) < 0) {
      String message = messageSource.getMessage(
          "error.user.insufficient.funds", new Object[]{client.getEmail()},
          LocaleContextHolder.getLocale());
      throw new InsufficientFundsException(message);
    }

    order.setEmployee(employee);
    orderRepository.save(order);
    orderStatusRecord.setStatus(OrderStatus.CONFIRMED);
    orderStatusRepository.save(orderStatusRecord);
    client.setBalance(client.getBalance().subtract(order.getPrice()));
    clientRepository.save(client);
    if (cacheManager.getCache("clients") != null) {
      cacheManager.getCache("clients").evict(client.getEmail());
    }
    log.info("Order {} confirmed successfully", orderId);
  }

  /**
   * {@inheritDoc}
   *
   * @throws NotFoundException     if order-related entities are not found
   * @throws IllegalStateException if order is not in PENDING state
   */
  @Override
  @Transactional
  public void cancelOrder(Long orderId, String employeeEmail) {
    log.info("Attempting to cancel order with id {}", orderId);

    OrderStatusRecord orderStatusRecord = orderStatusRepository.findByOrderId(orderId)
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              "error.order.status.not.found", new Object[]{orderId},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });

    if (orderStatusRecord.getStatus() != OrderStatus.PENDING) {
      String message = messageSource.getMessage(
          "error.order.status.not.pending", new Object[]{},
          LocaleContextHolder.getLocale());
      throw new IllegalStateException(message);
    }

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              "error.order.not.found", new Object[]{orderId},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });

    Employee employee = employeeRepository.findByEmail(employeeEmail)
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              "error.user.not.found", new Object[]{employeeEmail},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });

    orderStatusRecord.setStatus(OrderStatus.CANCELED);
    orderStatusRepository.save(orderStatusRecord);
    order.setEmployee(employee);
    orderRepository.save(order);
    log.info("Order {} canceled successfully", orderId);
  }

  /**
   * Maps an {@link Order} entity to a display-ready DTO, incorporating
   * its current lifecycle status.
   */
  private OrderDisplayDto mapToDisplayDto(Order order) {
    OrderDisplayDto dto = mapper.map(order, OrderDisplayDto.class);
    OrderStatus status = orderStatusRepository.findByOrderId(order.getId())
        .map(OrderStatusRecord::getStatus)
        .orElse(OrderStatus.PENDING);
    dto.setStatus(status);
    dto.setId(order.getId());
    return dto;
  }
}
