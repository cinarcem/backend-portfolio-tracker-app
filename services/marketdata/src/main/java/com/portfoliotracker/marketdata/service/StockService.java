package com.portfoliotracker.marketdata.service;


import com.portfoliotracker.marketdata.dto.StockResponse;

import java.util.List;
import java.util.Map;

public interface StockService {

    List<String> getAllStockSymbols();
    Map<String, StockResponse> getStocksMarketData(List<String> stockSymbols);

}
