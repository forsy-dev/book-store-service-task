package com.forsy.service;

import com.forsy.dto.CreateOrderRequestDto;
import com.forsy.dto.OrderDisplayDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining the business logic for managing bookstore orders.
 *
 * <p>This interface serves as the orchestrator for the bookstore's commercial
 * operations. It manages the full lifecycle of an order, including creation,
 * employee assignment, financial validation, and state transitions from
 * pending to confirmed or canceled.
 *
 * @author Illia
 */
public interface OrderService {

  /**
   * Retrieves a paginated list of orders associated with a specific client.
   *
   * @param clientEmail the unique email address of the client
   * @param pageable    the pagination and sorting parameters
   * @param keyword     an optional search term to filter orders by book titles
   *                    or other order details
   * @return a {@link Page} of {@link OrderDisplayDto} objects matching
   *     the criteria
   */
  Page<OrderDisplayDto> getOrdersByClient(String clientEmail, Pageable pageable, String keyword);

  /**
   * Retrieves a paginated list of orders managed by a specific employee.
   *
   * @param employeeEmail the unique professional email of the employee
   * @param pageable      the pagination and sorting parameters
   * @param keyword       an optional search term to filter the employee's
   *                      assigned orders
   * @return a {@link Page} of {@link OrderDisplayDto} objects matching
   *     the criteria
   */
  Page<OrderDisplayDto> getOrdersByEmployee(String employeeEmail, Pageable pageable,
      String keyword);

  /**
   * Retrieves a paginated list of all orders within the system.
   *
   * @param pageable the pagination and sorting parameters
   * @param keyword  an optional search term to filter across all orders
   * @return a {@link Page} of {@link OrderDisplayDto} objects matching
   *     the criteria
   */
  Page<OrderDisplayDto> getAllOrders(Pageable pageable, String keyword);

  /**
   * Initiates a new order within the bookstore system.
   *
   * <p>This operation calculates the total cost based on current book prices,
   * establishes links between books and the order, and initializes the
   * order in a PENDING status.
   *
   * @param order the data transfer object containing the order details,
   *              including items and participant emails
   * @return the {@link OrderDisplayDto} of the newly created pending order
   * @throws com.forsy.exception.NotFoundException if the client, employee, or any requested
   *                                               books are not found in the repository
   */
  OrderDisplayDto addOrder(CreateOrderRequestDto order);

  /**
   * Confirms a pending order and executes the financial transaction.
   *
   * <p>This ritual verifies that the order is in the correct state, ensures
   * the client has sufficient balance, deducts the price from the client's
   * treasury, and updates the status to CONFIRMED.
   *
   * @param orderId       the unique identifier of the order to be confirmed
   * @param employeeEmail the email of the employee authorizing the confirmation
   * @throws com.forsy.exception.NotFoundException          if the order, employee, or status record
   *                                                        does not exist
   * @throws IllegalStateException                          if the order is not in a PENDING state
   * @throws com.forsy.exception.InsufficientFundsException if the client's
   *                                                        balance is lower than the order price
   */
  void confirmOrder(Long orderId, String employeeEmail);

  /**
   * Cancels a pending order, preventing any financial deduction.
   *
   * <p>Updates the order state to CANCELED and records the employee
   * responsible for the cancellation.
   *
   * @param orderId       the unique identifier of the order to be canceled
   * @param employeeEmail the email of the employee authorizing the cancellation
   * @throws com.forsy.exception.NotFoundException if the order, employee, or status record
   *                                               does not exist
   * @throws IllegalStateException                 if the order is not in a PENDING state
   */
  void cancelOrder(Long orderId, String employeeEmail);
}
