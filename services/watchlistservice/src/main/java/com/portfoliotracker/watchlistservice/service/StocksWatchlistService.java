package com.portfoliotracker.watchlistservice.service;

import com.portfoliotracker.watchlistservice.dto.response.StockResultResponse;
import com.portfoliotracker.watchlistservice.dto.response.StockWithMarketDataResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface StocksWatchlistService {

    List<StockResultResponse> addStockToWatchlist(String userId, List<String>stockSymbols);
    Page<StockWithMarketDataResponse> getStockWatchlists(String userId, int page, int size, Sort sort);

}
