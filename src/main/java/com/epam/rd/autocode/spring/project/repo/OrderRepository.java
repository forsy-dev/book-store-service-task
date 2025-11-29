package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByClientEmail(String clientEmail, Pageable pageable);
    Page<Order> findAllByEmployeeEmail(String employeeEmail, Pageable pageable);
    @Query("SELECT o FROM Order o WHERE " +
        "CAST(o.id AS string) LIKE %:keyword% OR " +
        "LOWER(o.client.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(o.employee.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.client.email = :email AND " +
        "(CAST(o.id AS string) LIKE %:keyword%)")
    Page<Order> searchByClient(@Param("email") String email, @Param("keyword") String keyword, Pageable pageable);

    // Employee Assignment Search
    @Query("SELECT o FROM Order o WHERE o.employee.email = :email AND " +
        "(CAST(o.id AS string) LIKE %:keyword%)")
    Page<Order> searchByEmployee(@Param("email") String email, @Param("keyword") String keyword, Pageable pageable);
}
