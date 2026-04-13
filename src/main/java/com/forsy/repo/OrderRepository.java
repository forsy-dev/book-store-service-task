package com.forsy.repo;

import com.forsy.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing persistent {@link Order} entities.
 *
 * <p>This interface serves as the primary data access layer for transaction
 * history. It features advanced querying capabilities, including cross-entity
 * searching via JPQL and keyword-based filtering across order IDs and email
 * addresses, all while supporting full pagination.
 *
 * @author Illia
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * Retrieves a paginated list of all orders associated with a specific client.
   *
   * @param clientEmail the email address of the client to filter by
   * @param pageable    the pagination and sorting information
   * @return a {@link Page} of orders belonging to the specified client
   */
  Page<Order> findAllByClientEmail(String clientEmail, Pageable pageable);

  /**
   * Retrieves a paginated list of all orders processed by a specific employee.
   *
   * @param employeeEmail the professional email address of the employee to filter by
   * @param pageable      the pagination and sorting information
   * @return a {@link Page} of orders managed by the specified employee
   */
  Page<Order> findAllByEmployeeEmail(String employeeEmail, Pageable pageable);

  /**
   * Performs a global search across orders using a keyword.
   *
   * <p>The search checks for partial matches within the Order ID, the client's
   * email, and the employee's email.
   *
   * @param keyword  the search term (case-insensitive for emails)
   * @param pageable the pagination and sorting information
   * @return a {@link Page} of orders matching the keyword criteria
   */
  @Query("SELECT o FROM Order o WHERE "
      + "CAST(o.id AS string) LIKE %:keyword% OR "
      + "LOWER(o.client.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
      + "LOWER(o.employee.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);

  /**
   * Performs a targeted search for orders belonging to a specific client.
   *
   * @param email    the exact email of the client
   * @param keyword  the partial string to match against the Order ID
   * @param pageable the pagination and sorting information
   * @return a {@link Page} of matching client-specific orders
   */
  @Query("SELECT o FROM Order o WHERE o.client.email = :email AND "
      + "(CAST(o.id AS string) LIKE %:keyword%)")
  Page<Order> searchByClient(@Param("email") String email, @Param("keyword") String keyword,
                             Pageable pageable);

  /**
   * Performs a targeted search for orders managed by a specific employee.
   *
   * @param email    the professional email of the employee
   * @param keyword  the partial string to match against the Order ID
   * @param pageable the pagination and sorting information
   * @return a {@link Page} of matching employee-specific orders
   */
  @Query("SELECT o FROM Order o WHERE o.employee.email = :email AND "
      + "(CAST(o.id AS string) LIKE %:keyword%)")
  Page<Order> searchByEmployee(@Param("email") String email, @Param("keyword") String keyword,
                               Pageable pageable);

  /**
   * Removes all order records associated with a specific client email.
   *
   * <p>Warning: This operation is destructive and should be used in
   * accordance with data retention policies.
   *
   * @param clientEmail the email of the client whose orders are to be deleted
   */
  void deleteAllByClientEmail(String clientEmail);
}
