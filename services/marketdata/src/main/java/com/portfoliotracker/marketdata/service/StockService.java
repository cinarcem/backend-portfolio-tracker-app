package com.portfoliotracker.marketdata.service;


import com.portfoliotracker.marketdata.model.Stock;

import java.util.List;
import java.util.Map;

public interface StockService {

    List<String> getAllStockSymbols();
    Map<String, Stock> getStocksMarketData(List<String> stockSymbols);

}
