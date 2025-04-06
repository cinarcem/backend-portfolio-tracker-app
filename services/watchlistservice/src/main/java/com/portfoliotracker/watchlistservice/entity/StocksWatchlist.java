package com.portfoliotracker.watchlistservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "stocks_watchlist")
public class StocksWatchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @CreationTimestamp
    private LocalDateTime dateCreated;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String stockSymbol;
}
