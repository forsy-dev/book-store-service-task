package com.epam.rd.autocode.spring.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Client client;
    private Employee employee;
    private LocalDateTime orderDate;
    private BigDecimal price;
    private List<BookItem> bookItems;
}
