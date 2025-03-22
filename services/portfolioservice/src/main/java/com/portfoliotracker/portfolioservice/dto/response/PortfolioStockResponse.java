package com.portfoliotracker.portfolioservice.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PortfolioStockResponse {

    @NotNull(message = "stockSymbol cannot be null")
    private String stockSymbol;
    private BigDecimal latestPrice;
    private BigDecimal dailyChangePct;
    private BigDecimal dailyChangeInTL;
    private BigDecimal averageCost;
    private BigDecimal profitLossPct;
    private BigDecimal profitLossInTL;
    @NotNull(message = "quantity cannot be null")
    private BigDecimal quantity;
}
