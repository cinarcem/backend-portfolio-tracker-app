package com.portfoliotracker.portfolioservice.service;

import com.portfoliotracker.portfolioservice.dto.response.StockMarketDataResponse;

import java.util.List;
import java.util.Map;

public interface MarketDataService {

    boolean isValidStockSymbol(String symbol);
    Map<String, StockMarketDataResponse> fetchStocksMarketData(List<String> stockSymbols);

}
