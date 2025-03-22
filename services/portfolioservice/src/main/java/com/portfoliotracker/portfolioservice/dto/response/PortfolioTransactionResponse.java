package com.portfoliotracker.portfolioservice.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PortfolioTransactionResponse {

    private long id;
    @NotNull(message = "userId cannot be null")
    private String userId;
    @NotNull(message = "Date cannot be null")
    private LocalDate date;
    @NotNull(message = "stockSymbol cannot be null")
    private String stockSymbol;
    @Positive(message = "Quantity ust be greater than 0")
    private Long quantity;
    @PositiveOrZero(message = "Price must be zero or positive")
    private BigDecimal price;
}
