package com.portfoliotracker.watchlistservice.service;

import com.portfoliotracker.watchlistservice.dto.response.IndexWithMarketDataResponse;
import com.portfoliotracker.watchlistservice.dto.response.IndexResultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface IndexesWatchlistService {

    List<IndexResultResponse> addIndexToWatchlist(String userId, List<String>indexSymbols);
    Page<IndexWithMarketDataResponse> getWatchlistIndexes(String userId, int page, int size, Sort sort);

}
