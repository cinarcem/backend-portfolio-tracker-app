package com.portfoliotracker.watchlistservice.service.impl;

import com.portfoliotracker.watchlistservice.dto.response.StockResultResponse;
import com.portfoliotracker.watchlistservice.dto.response.StockWithMarketDataResponse;
import com.portfoliotracker.watchlistservice.entity.StocksWatchlist;
import com.portfoliotracker.watchlistservice.exception.ResourceNotFoundException;
import com.portfoliotracker.watchlistservice.repository.StocksWatchlistRepository;
import com.portfoliotracker.watchlistservice.service.MarketDataService;
import com.portfoliotracker.watchlistservice.service.StocksWatchlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StocksWatchlistServiceImpl implements StocksWatchlistService {

    private final StocksWatchlistRepository stocksWatchlistRepository;
    private final MarketDataService marketDataService;

    @Value("${SAMPLE_STOCKS:KOZAL,SASA}")
    private String sampleStockSymbols;

    /**
     * Adds a list of stock symbols to the specified user's watchlist.
     *
     * @param userId the ID of the user
     * @param stockSymbols the list of stock symbols to add
     * @return a list of results for each stock symbol, indicating success or failure
     */
    @Override
    public List<StockResultResponse> addStockToWatchlist(String userId, List<String> stockSymbols) {
        List<StockResultResponse> stockResultResponses  = new ArrayList<>();

        for(String stocksymbol : stockSymbols){
            stockResultResponses.add(this.stockSymbolAddingProcess(userId, stocksymbol));
        }

        return stockResultResponses;
    }

    /**
     * Retrieves the user's watchlist stocks enriched with market data and paginated.
     *
     * @param userId the ID of the user
     * @param page the page number for pagination
     * @param size the page size for pagination
     * @param sort the sorting criteria
     * @return a paginated list of stocks in the user's watchlist with market data
     * @throws ResourceNotFoundException if the user has no stocks in their watchlist
     */
    @Override
    public Page<StockWithMarketDataResponse> getStockWatchlists(String userId, int page, int size, Sort sort) {
        List<StocksWatchlist> userWatchlistStocks = stocksWatchlistRepository.findByUserId(userId);
        List<String> stockSymbols = userWatchlistStocks.stream()
                .map(StocksWatchlist::getStockSymbol)
                .toList();

        if(stockSymbols.isEmpty()){
            throw new ResourceNotFoundException("Watchlist stocks","userId",userId);
        }

        return marketDataService.fetchStocksMarketData(stockSymbols, page, size, sort);
    }


    /**
     * Deletes a list of stock symbols from user's watchlist.
     *
     * @param userId  the unique identifier of the user
     * @param stockSymbols the list of stock symbols to be deleted
     * @return a list of deleted stock symbols
     */
    @Transactional
    @Override
    public List<StockResultResponse> deleteStocksFromWatchlist(String userId, List<String> stockSymbols) {
        List<StockResultResponse> deletedStocksFromWatchlist = new ArrayList<>();

        for (String symbol : stockSymbols) {
            deletedStocksFromWatchlist.add(this.stockSymbolDeletingProcess(userId, symbol));
        }

        return deletedStocksFromWatchlist;
    }

    /**
     * Retrieves the sample stock watchlist stocks enriched with market data and paginated.
     *
     * @param page the page number for pagination
     * @param size the page size for pagination
     * @param sort the sorting criteria
     * @return a paginated list of sample stock watchlist with market data
     */
    @Override
    public Page<StockWithMarketDataResponse> getSampleStockWatchlist(int page, int size, Sort sort) {

        List<String> stockSymbols = List.of(sampleStockSymbols.split(","));

        return marketDataService.fetchStocksMarketData(stockSymbols, page, size, sort);
    }

    /**
     * Internal helper method to process a single stock symbol for watchlist addition.
     * Validates the symbol and prevents duplicates.
     *
     * @param userId the ID of the user
     * @param symbol the stock symbol to add
     * @return the result of the add operation, including any validation errors
     */
    private StockResultResponse stockSymbolAddingProcess(String userId, String symbol) {

        String status = "success";
        String error = null;

        Set<String> existingStocks = new HashSet<>(stocksWatchlistRepository.findStockSymbolsByUserId(userId));
        List<String> validStockSymbols = marketDataService.getAllStockSymbols();


        if (!validStockSymbols.contains(symbol)) {
            status = "failed";
            error = String.format("'%s' is not valid stock symbol.", symbol);
        }
        else if (existingStocks.contains(symbol)) {
            error = "Already exists.";
        }
        else {
            stocksWatchlistRepository.save(
                    StocksWatchlist.builder()
                            .userId(userId)
                            .stockSymbol(symbol)
                            .build()
            );
        }

        return StockResultResponse.builder()
                .stockSymbol(symbol)
                .status(status)
                .error(error)
                .build();
    }

    /**
     * Handles the logic for deleting a single stock symbol from the watchlist.
     *
     * @param userId the user to whom the stock will be deleted from watchlist
     * @param stockSymbol the stock symbol to be deleted
     * @return the result of the operation for this stock symbol
     */
    private StockResultResponse stockSymbolDeletingProcess(String userId, String stockSymbol) {

        String status = "success";
        String error = null;

        Set<String> existingStocks = new HashSet<>(stocksWatchlistRepository.findStockSymbolsByUserId(userId));

        if(existingStocks.contains(stockSymbol)){
            try {
                stocksWatchlistRepository.deleteByUserIdAndStockSymbol(userId, stockSymbol);
            }catch (Exception e){
                status = "failed";
                error = e.getMessage();
            }
        } else {
            status = "success";
            error = "Already not existing in the watchlist.";
        }

        return StockResultResponse.builder()
                .stockSymbol(stockSymbol)
                .status(status)
                .error(error)
                .build();
    }

}
