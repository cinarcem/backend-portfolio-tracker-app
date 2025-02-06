package com.portfoliotracker.marketdata.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Stock {

    private String stockSymbol;
    private BigDecimal latestPrice;
    private BigDecimal dailyChangePct;
    private BigDecimal dailyChangeInTL;
    private BigDecimal tradingVolumeTL;
    private BigDecimal tradeVolumeCount;
}
