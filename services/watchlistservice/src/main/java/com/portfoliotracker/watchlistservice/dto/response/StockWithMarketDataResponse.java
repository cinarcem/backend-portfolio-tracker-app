package com.portfoliotracker.watchlistservice.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StockWithMarketDataResponse {

    @NotBlank(message = "stockSymbol cannot be blank")
    private String stockSymbol;
    private BigDecimal latestValue;
    private BigDecimal dailyChangePct;
}
