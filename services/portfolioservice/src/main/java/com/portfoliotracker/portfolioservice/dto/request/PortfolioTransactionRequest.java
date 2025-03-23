package com.portfoliotracker.portfolioservice.dto.request;

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
@Builder
public class PortfolioTransactionRequest {

    @NotNull(message = "Date cannot be null")
    private LocalDate date;
    @NotNull(message = "stockSymbol cannot be null")
    private String stockSymbol;
    @Positive(message = "Quantity must be greater than 0")
    private Long quantity;
    @PositiveOrZero(message = "Price must be zero or positive")
    private BigDecimal price;

}
