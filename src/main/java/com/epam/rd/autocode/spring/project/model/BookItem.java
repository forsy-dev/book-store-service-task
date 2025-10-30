package com.epam.rd.autocode.spring.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookItem {

    private Long id;

    private Order order;

    private Book book;

    private Integer quantity;
}
