package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ORDERS")
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "{NotNull.invalid}")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "{NotNull.invalid}")
    private Employee employee;

    @Column(name = "ORDER_DATE", nullable = false)
    @NotNull(message = "{NotNull.invalid}")
    @PastOrPresent(message = "{PastOrPresent.invalid}")
    private LocalDateTime orderDate;

    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "{NotNull.invalid}")
    @DecimalMin(value = "0.01", message = "{DecimalMin.invalid}")
    private BigDecimal price;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @NotEmpty(message = "{NotEmpty.Order.bookItems}")
    private List<BookItem> bookItems;
}
