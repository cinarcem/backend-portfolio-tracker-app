package com.portfoliotracker.watchlistservice.service;

import com.portfoliotracker.watchlistservice.dto.response.IndexWithMarketDataResponse;
import com.portfoliotracker.watchlistservice.dto.response.StockWithMarketDataResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public interface MarketDataService {

    Set<String> getAllIndexSymbols();
    Page<IndexWithMarketDataResponse> fetchIndexesMarketData(List<String> indexSymbols, int page, int size, Sort sort);
    List<String> getAllStockSymbols();
    Page<StockWithMarketDataResponse> fetchStocksMarketData(List<String> stockSymbols, int page, int size, Sort sort);
}
