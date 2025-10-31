package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "CLIENTS")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Client extends User {

    @Column(name = "BALANCE", nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    public Client(Long id, String name, String email, String password, BigDecimal balance) {
        super(id, name, email, password);
        this.balance = balance;
    }
}
