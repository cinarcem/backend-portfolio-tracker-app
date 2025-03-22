package com.portfoliotracker.portfolioservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "portfolio_transactions")
public class PortfolioTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @CreationTimestamp
    private LocalDateTime dateCreated;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private String stockSymbol;
    @Column(nullable = false)
    @Positive(message = "Quantity must be greater than 0")
    private Long quantity;
    @Column(nullable = false)
    @PositiveOrZero(message = "Price must be zero or positive")
    private BigDecimal price;
}