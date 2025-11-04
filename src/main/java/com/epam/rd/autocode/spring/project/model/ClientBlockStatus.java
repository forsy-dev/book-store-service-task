package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CLIENT_BLOCK_STATUS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientBlockStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CLIENT_EMAIL", nullable = false, unique = true)
    private String clientEmail;

    @Column(name = "IS_BLOCKED", nullable = false)
    @Builder.Default
    private boolean isBlocked = false;
}
