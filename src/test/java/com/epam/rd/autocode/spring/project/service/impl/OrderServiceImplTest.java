package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.CreateOrderRequestDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
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
    private BookRepository bookRepository;

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

    @Nested
    class AddOrder {

        @Test
        void testAddOrder_ShouldReturnOrder() {
            String employeeEmail = "employee@test.com";
            Employee employee = Employee.builder().email(employeeEmail).build();

            String clientEmail = "client@test.com)";
            BigDecimal clientOldBalance = BigDecimal.TEN;
            BigDecimal clientNewBalance = BigDecimal.ZERO;
            Client client = Client.builder().email(clientEmail).balance(clientOldBalance).build();

            String bookName = "bookName";
            BigDecimal bookPrice = BigDecimal.TEN;
            Book book = Book.builder().name(bookName).price(bookPrice).build();

            BookItemDTO bookItemDto = BookItemDTO.builder().bookName(bookName).quantity(1).build();
            List<BookItemDTO> bookItems = Arrays.asList(bookItemDto);

            BookItem bookItem = BookItem.builder().book(book).quantity(1).build();
            List<BookItem> bookItemsList = Arrays.asList(bookItem);

            CreateOrderRequestDTO orderDTO = CreateOrderRequestDTO.builder()
                    .employeeEmail(employeeEmail)
                    .clientEmail(clientEmail)
                    .bookItems(bookItems)
                    .build();

            Client savedClient = Client.builder().email(clientEmail).balance(clientNewBalance).build();
            Order order = Order.builder().client(client).employee(employee).price(bookPrice).bookItems(bookItemsList).build();
            OrderDTO expectedDto = OrderDTO.builder().build();

            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.of(employee));
            when(clientRepository.findByEmail(clientEmail)).thenReturn(Optional.of(client));
            when(bookRepository.findByName(bookName)).thenReturn(Optional.of(book));
            when(clientRepository.save(client)).thenReturn(savedClient);
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(mapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(expectedDto);

            orderService.addOrder(orderDTO);

            verify(employeeRepository, times(1)).findByEmail(employeeEmail);
            verify(clientRepository, times(1)).findByEmail(clientEmail);
            verify(bookRepository, times(1)).findByName(bookName);
            verify(clientRepository, times(1)).save(client);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(mapper, times(1)).map(any(Order.class), eq(OrderDTO.class));
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

        @Test
        void testAddOrder_ShouldThrowExceptionWhenInsufficientFunds() {
            String employeeEmail = "employee@test.com";
            Employee employee = Employee.builder().email(employeeEmail).build();

            String clientEmail = "client@test.com)";
            BigDecimal clientOldBalance = BigDecimal.ZERO;
            Client client = Client.builder().email(clientEmail).balance(clientOldBalance).build();

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

            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.of(employee));
            when(clientRepository.findByEmail(clientEmail)).thenReturn(Optional.of(client));
            when(bookRepository.findByName(bookName)).thenReturn(Optional.of(book));

            assertThrows(InsufficientFundsException.class, () -> orderService.addOrder(orderDTO));

            verify(employeeRepository, times(1)).findByEmail(employeeEmail);
            verify(clientRepository, times(1)).findByEmail(clientEmail);
            verify(bookRepository, times(1)).findByName(bookName);
            verify(clientRepository, never()).save(any(Client.class));
            verify(orderRepository, never()).save(any(Order.class));
            verify(mapper, never()).map(any(Order.class), any());
        }
    }
}
