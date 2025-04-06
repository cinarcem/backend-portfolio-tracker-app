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
public class IndexWithMarketDataResponse {

    @NotBlank(message = "indexSymbol cannot be blank")
    private String indexSymbol;
    private BigDecimal latestValue;
    private BigDecimal dailyChangePct;
}
