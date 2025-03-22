package com.portfoliotracker.portfolioservice.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StockMarketDataResponse {

    private String stockSymbol;
    private BigDecimal latestPrice;
    private BigDecimal dailyChangePct;
    private BigDecimal dailyChangeInTL;
    private BigDecimal tradingVolumeTL;
    private BigDecimal tradeVolume;
}
