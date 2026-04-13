package com.forsy.controller;

import com.forsy.dto.BookItemDto;
import com.forsy.dto.CreateOrderRequestDto;
import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.OrderDisplayDto;
import com.forsy.service.EmployeeService;
import com.forsy.service.OrderService;
import com.forsy.util.CartCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsible for processing bookstore orders and transaction history.
 *
 * <p>Handles order placement for clients and order management (confirmation and
 * cancellation) for employees. It provides filtered views of order history based
 * on the user's role and identity, ensuring strict data isolation.
 *
 * @author Illia
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

  private final OrderService orderService;
  private final EmployeeService employeeService;
  private final CartCookieUtil cartCookieUtil;
  private final MessageSource messageSource;

  /**
   * Retrieves a paginated list of all orders in the system.
   *
   * <p>If a client accesses this endpoint, they are automatically redirected to
   * their personal order history. Employees can view all orders, optionally
   * filtered by a keyword.
   *
   * @param model          the Spring MVC model to populate with order data
   * @param authentication the current user's authentication and roles
   * @param pageable       pagination and sorting details (defaults to newest orders first)
   * @param keyword        an optional search string for filtering orders
   * @return the orders view name or a redirect for clients
   */
  @GetMapping
  public String getAllOrders(Model model,
                             Authentication authentication,
                             @PageableDefault(sort = "orderDate",
                                 direction = org.springframework.data.domain.Sort.Direction.DESC)
                             Pageable pageable,
                             @RequestParam(name = "keyword", required = false) String keyword) {

    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
      return "redirect:/orders/" + authentication.getName();
    }
    if (keyword != null && keyword.trim().isEmpty()) {
      keyword = null;
    }
    log.info("Fetching ALL orders for employee: {}", authentication.getName());
    Page<OrderDisplayDto> orders = orderService.getAllOrders(pageable, keyword);
    model.addAttribute("orderPage", orders);
    model.addAttribute("keyword", keyword);
    model.addAttribute("baseSearchUrl", "/orders");
    model.addAttribute("pageTitle", "nav.all_orders");

    return "orders";
  }

  /**
   * Retrieves the order history for a specific user identified by email.
   *
   * <p>Enforces strict security: users can only view their own orders.
   *
   * @param model          the Spring MVC model to populate with specific order data
   * @param email          the email address of the user whose orders are being fetched
   * @param authentication the current user's authentication details
   * @param pageable       pagination and sorting details
   * @param keyword        an optional search string for filtering orders
   * @return the orders view name
   * @throws AccessDeniedException if a user attempts to view another person's orders
   */
  @GetMapping("/{email}")
  public String getOrdersForUser(Model model,
                                 @PathVariable String email,
                                 Authentication authentication,
                                 @PageableDefault(sort = "orderDate", direction =
                                     org.springframework.data.domain.Sort.Direction.DESC)
                                 Pageable pageable,
                                 @RequestParam(name = "keyword", required = false) String keyword) {

    if (!authentication.getName().equals(email)) {
      log.warn("User {} attempted to view orders for {}", authentication.getName(), email);
      throw new AccessDeniedException("You are not authorized to view these orders.");
    }
    if (keyword != null && keyword.trim().isEmpty()) {
      keyword = null;
    }

    log.info("Fetching orders for user: {}", email);
    Page<OrderDisplayDto> orders;

    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
      orders = orderService.getOrdersByClient(email, pageable, keyword);
    } else {
      orders = orderService.getOrdersByEmployee(email, pageable, keyword);
    }

    model.addAttribute("orderPage", orders);
    model.addAttribute("keyword", keyword);
    model.addAttribute("baseSearchUrl", "/orders/" + email);
    model.addAttribute("pageTitle", "nav.my_orders");
    return "orders";
  }

  /**
   * Processes the submission of the shopping cart into a formalized order.
   *
   * <p>Converts the current cart cookie into a series of book items, assigns an
   * available employee to the order, and clears the cart cookie upon successful creation.
   *
   * @param authentication     the current client's authentication details
   * @param request            the current HTTP request, used to retrieve the cart cookie
   * @param response           the HTTP response, used to clear the cart cookie on success
   * @param redirectAttributes used to pass success or error messages to the next page
   * @return a redirect to the catalog on success or the cart on failure
   */
  @PostMapping("/submit")
  public String submitOrder(Authentication authentication,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {

    String clientEmail = authentication.getName();
    log.info("Client {} is attempting to submit an order.", clientEmail);

    Map<String, Integer> cart = cartCookieUtil.getCartFromCookie(request);
    if (cart == null || cart.isEmpty()) {
      redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty.");
      return "redirect:/cart";
    }

    List<BookItemDto> bookItems = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : cart.entrySet()) {
      bookItems.add(new BookItemDto(entry.getKey(), entry.getValue()));
    }

    Page<EmployeeDisplayDto> employees = employeeService.getAllEmployees(PageRequest.of(0, 1));
    if (employees.isEmpty()) {
      log.error("Order creation failed: No employees exist to assign the order to.");
      redirectAttributes.addFlashAttribute("errorMessage",
          "System error: No employees available to process order.");
      return "redirect:/cart";
    }
    String assigneeEmail = employees.getContent().get(0).getEmail();

    CreateOrderRequestDto req = CreateOrderRequestDto.builder()
        .clientEmail(clientEmail)
        .employeeEmail(assigneeEmail)
        .orderDate(LocalDateTime.now())
        .bookItems(bookItems)
        .build();

    try {
      OrderDisplayDto createdOrder = orderService.addOrder(req);
      log.info("Order created successfully. ID/Details: {}", createdOrder);

      cartCookieUtil.deleteCartCookie(response);

      String message = messageSource.getMessage("order.submit.success.message",
          new Object[]{}, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("successMessage", message);
      return "redirect:/books";

    } catch (Exception e) {
      log.warn("Failed to create order: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/cart";
    }
  }

  /**
   * Cancels an existing order by its unique identifier.
   *
   * @param id                 the unique ID of the order to cancel
   * @param authentication     the current employee's authentication details
   * @param redirectAttributes used to pass success or error messages to the next page
   * @return a redirect to the general orders history
   */
  @PostMapping("/{id}/cancel")
  public String cancelOrder(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
    String employeeEmail = authentication.getName();
    log.info("Employee {} canceling order {}", employeeEmail, id);
    try {
      orderService.cancelOrder(id, employeeEmail);
      String message = messageSource.getMessage("order.cancel.success.message",
          new Object[]{id}, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("successMessage", message);
    } catch (Exception e) {
      log.warn("Failed to cancel order {}: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    log.info("Order {} cancelled successfully", id);
    return "redirect:/orders";
  }

  /**
   * Confirms an existing order by its unique identifier.
   *
   * @param id                 the unique ID of the order to confirm
   * @param authentication     the current employee's authentication details
   * @param redirectAttributes used to pass success or error messages to the next page
   * @return a redirect to the general orders history
   */
  @PostMapping("/{id}/confirm")
  public String confirmOrder(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
    String employeeEmail = authentication.getName();
    log.info("Employee {} confirming order {}", employeeEmail, id);
    try {
      orderService.confirmOrder(id, employeeEmail);
      String message = messageSource.getMessage("order.confirm.success.message",
          new Object[]{id}, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("successMessage", message);
    } catch (Exception e) {
      log.warn("Failed to confirm order {}: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    log.info("Order {} confirmed successfully", id);
    return "redirect:/orders";
  }
}
