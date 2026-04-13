package com.forsy.repo;

import com.forsy.model.OrderStatusRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing persistent {@link OrderStatusRecord} entities.
 *
 * <p>This interface acts as the dedicated data access point for tracking
 * the current lifecycle state of an order. It provides the mechanism to
 * link business-level orders to their specific status markers within the
 * "ORDER_STATUS_REGISTRY" table.
 *
 * @author Illia
 */
@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatusRecord, Long> {

  /**
   * Retrieves the status record associated with a specific order identifier.
   *
   * <p>This is the primary lookup used to verify whether an order is
   * pending, confirmed, or canceled based on its unique primary key.
   *
   * @param orderId the unique identifier of the order whose status is being queried
   * @return an {@link Optional} containing the status record, or empty if none is found
   */
  Optional<OrderStatusRecord> findByOrderId(Long orderId);
}
