package com.portfoliotracker.watchlistservice.service.impl;

import com.portfoliotracker.watchlistservice.dto.response.IndexWithMarketDataResponse;
import com.portfoliotracker.watchlistservice.dto.response.IndexResultResponse;
import com.portfoliotracker.watchlistservice.entity.IndexesWatchlist;
import com.portfoliotracker.watchlistservice.exception.ResourceNotFoundException;
import com.portfoliotracker.watchlistservice.repository.IndexesWatchlistRepository;
import com.portfoliotracker.watchlistservice.service.IndexesWatchlistService;
import com.portfoliotracker.watchlistservice.service.MarketDataService;
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
public class IndexesWatchlistServiceImpl implements IndexesWatchlistService {

    private final IndexesWatchlistRepository indexesWatchlistRepository;
    private final MarketDataService marketDataService;

    @Value("${SAMPLE_INDEXES:XU100,XU030}")
    private String sampleIndexSymbols;

    /**
     * Adds a list of index symbols to the user's watchlist.
     *
     * For each index symbol, the method checks its validity, avoids duplicates,
     * and saves it to the watchlist if eligible.
     *
     * @param userId the unique identifier of the user
     * @param indexSymbols the list of index symbols to be added
     * @return a list of result responses for each attempted index addition
     */
    @Override
    public List<IndexResultResponse> addIndexToWatchlist(String userId, List<String> indexSymbols) {

        List<IndexResultResponse> indexResultResponses = new ArrayList<>();

        for (String symbol : indexSymbols) {
            indexResultResponses.add(this.indexSymbolAddingProcess(userId, symbol));
        }

        return indexResultResponses;

    }

    /**
     * Retrieves the user's watchlist of indexes along with market data.
     *
     * The method fetches all index symbols in the user's watchlist,
     * then uses the MarketDataService to return paginated market data.
     *
     * @param userId the unique identifier of the user
     * @param page the page number for pagination
     * @param size the number of items per page
     * @param sort sorting parameters for the result
     * @return a paginated list of watchlist indexes enriched with market data
     * @throws ResourceNotFoundException if the user has no indexes in their watchlist
     */
    @Override
    public Page<IndexWithMarketDataResponse> getWatchlistIndexes(String userId, int page, int size, Sort sort) {

        List<IndexesWatchlist> userWatchlistIndexes = indexesWatchlistRepository.findByUserId(userId);
        List<String> indexSymbols = userWatchlistIndexes.stream()
                .map(IndexesWatchlist::getIndexSymbol)
                .toList();

        if(indexSymbols.isEmpty()){
            throw new ResourceNotFoundException("Watchlist indexes","userId",userId);
        }

        return marketDataService.fetchIndexesMarketData(indexSymbols, page, size, sort);
    }

    /**
     * Retrieves the sample index watchlist stocks enriched with market data and paginated.
     *
     * @param page the page number for pagination
     * @param size the page size for pagination
     * @param sort the sorting criteria
     * @return a paginated list of sample index watchlist with market data
     */
    @Override
    public Page<IndexWithMarketDataResponse> getSampleIndexWatchlist(int page, int size, Sort sort) {

        List<String> indexSymbols = List.of(sampleIndexSymbols.split(","));

        return marketDataService.fetchIndexesMarketData(indexSymbols, page, size, sort);
    }

    /**
     * Deletes a list of index symbols from user's watchlist.
     *
     * @param userId  the unique identifier of the user
     * @param symbols the list of index symbols to be deleted
     * @return a list of deleted indexes
     */
    @Transactional
    @Override
    public List<IndexResultResponse> deleteIndexesFromWatchlist(String userId, List<String> symbols) {

        List<IndexResultResponse> deletedIndexesFromWatchlist = new ArrayList<>();

        for (String symbol : symbols) {
            deletedIndexesFromWatchlist.add(this.indexSymbolDeletingProcess(userId, symbol));
        }

        return deletedIndexesFromWatchlist;

    }

    /**
     * Handles the logic for adding a single index symbol to the watchlist.
     *
     * Validates the symbol against the known list, checks for duplicates,
     * and saves it if valid and not already added. Returns a result
     * containing status and any error messages.
     *
     * @param userId the user to whom the index will be added
     * @param symbol the index symbol to add
     * @return the result of the operation for this index symbol
     */
    private IndexResultResponse indexSymbolAddingProcess(String userId, String symbol) {

        String status = "success";
        String error = null;

        Set<String> existingIndexes = new HashSet<>(indexesWatchlistRepository.findIndexSymbolsByUserId(userId));
        Set<String> validIndexSymbols = marketDataService.getAllIndexSymbols();

        if (!validIndexSymbols.contains(symbol)) {
            status = "failed";
            error = String.format("'%s' is not valid index symbol.", symbol);
        }
        else if (existingIndexes.contains(symbol)) {
            error = "Already exists.";
        }
        else {
            indexesWatchlistRepository.save(
                    IndexesWatchlist.builder()
                            .userId(userId)
                            .indexSymbol(symbol)
                            .build()
            );
        }

        return IndexResultResponse.builder()
                .indexSymbol(symbol)
                .status(status)
                .error(error)
                .build();
    }

    /**
     * Handles the logic for deleting a single index symbol from the watchlist.
          *
     * @param userId the user to whom the index will be deleted from watchlist
     * @param symbol the index symbol to be deleted
     * @return the result of the operation for this index symbol
     */
    private IndexResultResponse indexSymbolDeletingProcess(String userId, String symbol) {

        String status = "success";
        String error = null;

        Set<String> existingIndexes = new HashSet<>(indexesWatchlistRepository.findIndexSymbolsByUserId(userId));

        if(existingIndexes.contains(symbol)){
            try {
                indexesWatchlistRepository.deleteByUserIdAndIndexSymbol(userId, symbol);
            }catch (Exception e){
                status = "failed";
                error = e.getMessage();
            }
        } else {
            status = "success";
            error = "Already not existing in the watchlist.";
        }

        return IndexResultResponse.builder()
                .indexSymbol(symbol)
                .status(status)
                .error(error)
                .build();
    }

}
