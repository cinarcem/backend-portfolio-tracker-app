package com.portfoliotracker.marketdata.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IndexResponse {

    private String symbol;
    private String name;
    private BigDecimal latestValue;
    private BigDecimal dailyChangePct;
    private BigDecimal weeklyChangePct;
    private BigDecimal monthlyChangePct;
    private BigDecimal yearlyChangePct;

}
