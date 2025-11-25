package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.*;
import org.junit.jupiter.api.Nested;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Mock
    OrderStatusRepository orderStatusRepository;

    @Mock
    private BookRepository bookRepository;

    @Test
    void testGetAllOrdersByClient_ShouldReturnPagedOrders() {
        String clientEmail = "test@test.com";
        Long orderId = 1L;
        Order order = Order.builder().id(orderId).build();
        OrderDisplayDTO expectedDto = new OrderDisplayDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(order), pageable, 1);

        OrderStatusRecord statusRecord = OrderStatusRecord.builder().status(OrderStatus.PENDING).build();

        when(orderRepository.findAllByClientEmail(clientEmail, pageable)).thenReturn(orderPage);
        when(mapper.map(order, OrderDisplayDTO.class)).thenReturn(expectedDto);
        when(orderStatusRepository.findByOrderId(orderId)).thenReturn(Optional.of(statusRecord));

        Page<OrderDisplayDTO> actualOrderDto = orderService.getOrdersByClient(clientEmail, pageable);

        verify(orderRepository, times(1)).findAllByClientEmail(clientEmail, pageable);
        verify(mapper, times(1)).map(order, OrderDisplayDTO.class);
        verify(orderStatusRepository, times(1)).findByOrderId(orderId);

        assertEquals(1, actualOrderDto.getTotalElements());
        assertEquals(1, actualOrderDto.getContent().size());
        assertEquals(expectedDto, actualOrderDto.getContent().get(0));
        assertEquals(OrderStatus.PENDING, actualOrderDto.getContent().get(0).getStatus());
    }

    @Test
    void testGetAllOrdersByEmployee_ShouldReturnPagedOrders() {
        String employeeEmail = "test@test.com";
        Long orderId = 1L;
        Order order = Order.builder().id(orderId).build();
        OrderDisplayDTO expectedDto = new OrderDisplayDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(order), pageable, 1);

        // Mock status
        OrderStatusRecord statusRecord = OrderStatusRecord.builder().status(OrderStatus.CONFIRMED).build();

        when(orderRepository.findAllByEmployeeEmail(employeeEmail, pageable)).thenReturn(orderPage);
        when(mapper.map(order, OrderDisplayDTO.class)).thenReturn(expectedDto);
        when(orderStatusRepository.findByOrderId(orderId)).thenReturn(Optional.of(statusRecord));

        Page<OrderDisplayDTO> actualOrderDto = orderService.getOrdersByEmployee(employeeEmail, pageable);

        verify(orderRepository, times(1)).findAllByEmployeeEmail(employeeEmail, pageable);
        verify(mapper, times(1)).map(order, OrderDisplayDTO.class);
        verify(orderStatusRepository, times(1)).findByOrderId(orderId);

        assertEquals(1, actualOrderDto.getTotalElements());
        assertEquals(1, actualOrderDto.getContent().size());
        assertEquals(expectedDto, actualOrderDto.getContent().get(0));
        assertEquals(OrderStatus.CONFIRMED, actualOrderDto.getContent().get(0).getStatus());
    }

    @Nested
    class AddOrder {

        @Test
        void testAddOrder_ShouldReturnOrder() {
            String employeeEmail = "employee@test.com";
            Employee employee = Employee.builder().email(employeeEmail).build();

            String clientEmail = "client@test.com)";
            Client client = Client.builder().email(clientEmail).build();

            String bookName = "bookName";
            BigDecimal bookPrice = BigDecimal.TEN;
            Book book = Book.builder().name(bookName).price(bookPrice).build();

            BookItemDTO bookItemDto = BookItemDTO.builder().bookName(bookName).quantity(1).build();
            List<BookItemDTO> bookItems = Arrays.asList(bookItemDto);

            CreateOrderRequestDTO orderDTO = CreateOrderRequestDTO.builder()
                    .employeeEmail(employeeEmail)
                    .clientEmail(clientEmail)
                    .bookItems(bookItems)
                    .build();

            Long orderId = 1L;
            Order order = Order.builder().id(orderId).client(client).employee(employee).price(bookPrice).build();
            OrderDisplayDTO expectedDto = OrderDisplayDTO.builder()
                    .clientEmail(clientEmail)
                    .employeeEmail(employeeEmail)
                    .build();

            OrderStatusRecord statusRecord = OrderStatusRecord.builder().status(OrderStatus.PENDING).build();

            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.of(employee));
            when(clientRepository.findByEmail(clientEmail)).thenReturn(Optional.of(client));
            when(bookRepository.findByName(bookName)).thenReturn(Optional.of(book));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderStatusRepository.findByOrderId(orderId)).thenReturn(Optional.of(statusRecord));
            when(mapper.map(any(Order.class), eq(OrderDisplayDTO.class))).thenReturn(expectedDto);

            OrderDisplayDTO actualDTO = orderService.addOrder(orderDTO);

            verify(employeeRepository, times(1)).findByEmail(employeeEmail);
            verify(clientRepository, times(1)).findByEmail(clientEmail);
            verify(bookRepository, times(1)).findByName(bookName);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(orderStatusRepository, times(1)).findByOrderId(orderId);
            verify(mapper, times(1)).map(any(Order.class), eq(OrderDisplayDTO.class));

            assertEquals(expectedDto, actualDTO);
        }

        @Test
        void testAddOrder_ShouldThrowExceptionWhenEmployeeNotFound() {
            String employeeEmail = "employee@test.com";

            CreateOrderRequestDTO orderDTO = CreateOrderRequestDTO.builder()
                    .employeeEmail(employeeEmail)
                    .build();

            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));

            verify(employeeRepository, times(1)).findByEmail(employeeEmail);
            verify(clientRepository, never()).findByEmail(anyString());
            verify(bookRepository, never()).findByName(anyString());
            verify(clientRepository, never()).save(any(Client.class));
            verify(orderRepository, never()).save(any(Order.class));
            verify(mapper, never()).map(any(Order.class), any());
        }

        @Test
        void testAddOrder_ShouldThrowExceptionWhenClientNotFound() {
            String employeeEmail = "employee@test.com";
            Employee employee = Employee.builder().email(employeeEmail).build();

            String clientEmail = "client@test.com)";

            CreateOrderRequestDTO orderDTO = CreateOrderRequestDTO.builder()
                    .employeeEmail(employeeEmail)
                    .clientEmail(clientEmail)
                    .build();

            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.of(employee));
            when(clientRepository.findByEmail(clientEmail)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));

            verify(employeeRepository, times(1)).findByEmail(employeeEmail);
            verify(clientRepository, times(1)).findByEmail(clientEmail);
            verify(bookRepository, never()).findByName(anyString());
            verify(clientRepository, never()).save(any(Client.class));
            verify(orderRepository, never()).save(any(Order.class));
            verify(mapper, never()).map(any(Order.class), any());
        }

        @Test
        void testAddOrder_ShouldThrowExceptionWhenBookNotFound() {
            String employeeEmail = "employee@test.com";
            Employee employee = Employee.builder().email(employeeEmail).build();

            String clientEmail = "client@test.com)";
            Client client = Client.builder().email(clientEmail).build();

            String bookName = "bookName";

            BookItemDTO bookItemDto = BookItemDTO.builder().bookName(bookName).build();
            List<BookItemDTO> bookItems = Arrays.asList(bookItemDto);

            CreateOrderRequestDTO orderDTO = CreateOrderRequestDTO.builder()
                    .employeeEmail(employeeEmail)
                    .clientEmail(clientEmail)
                    .bookItems(bookItems)
                    .build();

            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.of(employee));
            when(clientRepository.findByEmail(clientEmail)).thenReturn(Optional.of(client));
            when(bookRepository.findByName(bookName)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));

            verify(employeeRepository, times(1)).findByEmail(employeeEmail);
            verify(clientRepository, times(1)).findByEmail(clientEmail);
            verify(bookRepository, times(1)).findByName(bookName);
            verify(clientRepository, never()).save(any(Client.class));
            verify(orderRepository, never()).save(any(Order.class));
            verify(mapper, never()).map(any(Order.class), any());
        }
    }

    @Nested
    class GetAllOrders {

        @Test
        void testGetAllOrders_ShouldReturnOrders() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orders = new PageImpl<>(Arrays.asList(new Order()));

            when(orderRepository.findAll(pageable)).thenReturn(orders);
            when(mapper.map(any(Order.class), eq(OrderDisplayDTO.class))).thenReturn(new OrderDisplayDTO());

            orderService.getAllOrders(pageable);

            verify(orderRepository, times(1)).findAll(pageable);
            verify(mapper, times(1)).map(any(Order.class), eq(OrderDisplayDTO.class));
        }

        @Test
        void testGetAllOrders_ShouldReturnEmptyList_WhenOrdersNotFound() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orders = new PageImpl<>(Arrays.asList());

            when(orderRepository.findAll(pageable)).thenReturn(orders);

            orderService.getAllOrders(pageable);

            verify(orderRepository, times(1)).findAll(pageable);
            verify(mapper, never()).map(any(Order.class), eq(OrderDisplayDTO.class));
        }
    }
}
