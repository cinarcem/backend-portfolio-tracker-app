package com.portfoliotracker.marketdata.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StockResponse {

    private String stockSymbol;
    private BigDecimal latestPrice;
    private BigDecimal dailyChangePct;
    private BigDecimal dailyChangeInTL;
    private BigDecimal tradingVolumeTL;
    private BigDecimal tradeVolumeCount;
}
