package com.forsy.repo;

import com.forsy.model.OrderStatusRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatusRecord, Long> {
    Optional<OrderStatusRecord> findByOrderId(Long orderId);
}
