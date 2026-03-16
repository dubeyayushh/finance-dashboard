package com.finance.model;

import com.finance.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Stored as string "INCOME" / "EXPENSE" in DB instead of 0/1
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
}