package com.ivanfranchin.bitcoinapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long portfolioId;

    @Column(nullable = false)
    private String isin;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Long count;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public Transaction(Long portfolioId, String isin, String type, Long count, BigDecimal price, BigDecimal value, LocalDateTime timestamp) {
        this.portfolioId = portfolioId;
        this.isin = isin;
        this.type = type;
        this.count = count;
        this.price = price;
        this.value = value;
        this.timestamp = timestamp;
    }
}
