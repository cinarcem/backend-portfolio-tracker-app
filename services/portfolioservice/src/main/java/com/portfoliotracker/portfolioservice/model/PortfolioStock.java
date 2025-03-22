package com.portfoliotracker.portfolioservice.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PortfolioStock {

    private String stockSymbol;
    private Double averageCost;
    private Double quantity;
}
