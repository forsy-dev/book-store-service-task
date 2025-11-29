package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.util.CartCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final EmployeeService employeeService;
    private final ClientService clientService;
    private final CartCookieUtil cartCookieUtil;

    @GetMapping
    public String getAllOrders(Model model,
                               Authentication authentication,
                               @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
                               @RequestParam(name = "keyword", required = false) String keyword) {

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            return "redirect:/orders/" + authentication.getName();
        }
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        log.info("Fetching ALL orders for employee: {}", authentication.getName());
        Page<OrderDisplayDTO> orders = orderService.getAllOrders(pageable, keyword);
        model.addAttribute("orderPage", orders);
        model.addAttribute("keyword", keyword);
        model.addAttribute("baseSearchUrl", "/orders");
        model.addAttribute("pageTitle", "nav.all_orders");

        return "orders";
    }

    @GetMapping("/{email}")
    public String getOrdersForUser(Model model,
                                   @PathVariable String email,
                                   Authentication authentication,
                                   @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
                                   @RequestParam(name = "keyword", required = false) String keyword) {

        // Security Check for Clients
        if (!authentication.getName().equals(email)) {
            log.warn("User {} attempted to view orders for {}", authentication.getName(), email);
            throw new AccessDeniedException("You are not authorized to view these orders.");
        }
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        log.info("Fetching orders for user: {}", email);
        Page<OrderDisplayDTO> orders;

        // Determine if the email belongs to a Client or an Employee to call the correct service method
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            orders = orderService.getOrdersByClient(email, pageable, keyword);
        } else {
            // Assuming ROLE_EMPLOYEE
            orders = orderService.getOrdersByEmployee(email, pageable, keyword);
        }

        model.addAttribute("orderPage", orders);
        model.addAttribute("keyword", keyword);
        model.addAttribute("baseSearchUrl", "/orders/" + email);
        model.addAttribute("pageTitle", "nav.my_orders");
        return "orders";
    }


    @PostMapping("/submit")
    public String submitOrder(Authentication authentication,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {

        String clientEmail = authentication.getName();
        log.info("Client {} is attempting to submit an order.", clientEmail);

        // 1. Get Cart
        Map<String, Integer> cart = cartCookieUtil.getCartFromCookie(request);
        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty.");
            return "redirect:/cart";
        }

        // 2. Build BookItems List
        List<BookItemDTO> bookItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            bookItems.add(new BookItemDTO(entry.getKey(), entry.getValue()));
        }

        // 3. Auto-Assign an Employee (Workaround for DB constraint)
        // We pick the first employee found to be the "handler" of this order.
        Page<EmployeeDisplayDTO> employees = employeeService.getAllEmployees(PageRequest.of(0, 1));
        if (employees.isEmpty()) {
            log.error("Order creation failed: No employees exist to assign the order to.");
            redirectAttributes.addFlashAttribute("errorMessage", "System error: No employees available to process order.");
            return "redirect:/cart";
        }
        String assigneeEmail = employees.getContent().get(0).getEmail();

        // 4. Create the Request DTO
        CreateOrderRequestDTO req = CreateOrderRequestDTO.builder()
                .clientEmail(clientEmail)
                .employeeEmail(assigneeEmail)
                .orderDate(LocalDateTime.now())
                .bookItems(bookItems)
                .build();

        // 5. Call Service
        try {
            OrderDisplayDTO createdOrder = orderService.addOrder(req);
            log.info("Order created successfully. ID/Details: {}", createdOrder);

            // Clear the cart on success
            cartCookieUtil.deleteCartCookie(response);

            redirectAttributes.addFlashAttribute("successMessage", "Order placed successfully!");
            return "redirect:/books";

        } catch (Exception e) {
            log.warn("Failed to create order: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        String employeeEmail = authentication.getName();
        log.info("Employee {} canceling order {}", employeeEmail, id);
        try {
            // Pass the employee email to service to take ownership of the order
            orderService.cancelOrder(id, employeeEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Order " + id + " canceled.");
        } catch (Exception e) {
            log.warn("Failed to cancel order {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        log.info("Order {} cancelled successfully", id);
        return "redirect:/orders";
    }

    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        String employeeEmail = authentication.getName();
        log.info("Employee {} confirming order {}", employeeEmail, id);
        try {
            // Pass the employee email to service to take ownership of the order
            orderService.confirmOrder(id, employeeEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Order " + id + " confirmed.");
        } catch (Exception e) {
            log.warn("Failed to confirm order {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        log.info("Order {} confirmed successfully", id);
        return "redirect:/orders";
    }
}
