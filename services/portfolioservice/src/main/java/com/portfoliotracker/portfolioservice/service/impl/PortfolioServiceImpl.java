package com.portfoliotracker.portfolioservice.service.impl;

import com.portfoliotracker.portfolioservice.dto.request.PortfolioTransactionRequest;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioStockResponse;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioTransactionResponse;
import com.portfoliotracker.portfolioservice.dto.response.StockMarketDataResponse;
import com.portfoliotracker.portfolioservice.entity.PortfolioTransaction;
import com.portfoliotracker.portfolioservice.exception.InvalidSymbolsException;
import com.portfoliotracker.portfolioservice.exception.ResourceNotFoundException;
import com.portfoliotracker.portfolioservice.exception.ResourceNotDeletedException;
import com.portfoliotracker.portfolioservice.mapper.PortfolioTransactionMapper;
import com.portfoliotracker.portfolioservice.model.PortfolioStock;
import com.portfoliotracker.portfolioservice.repository.PortfolioTransactionRepository;
import com.portfoliotracker.portfolioservice.service.MarketDataService;
import com.portfoliotracker.portfolioservice.service.PortfolioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioTransactionRepository portfolioTransactionRepository;
    private final MarketDataService marketDataService;
    private final PortfolioTransactionMapper portfolioTransactionMapper;

    @Override
    public PortfolioTransactionResponse savePortfolioTransaction(String userId, PortfolioTransactionRequest portfolioTransactionRequest) {

        String stockSymbol = portfolioTransactionRequest.getStockSymbol();
        boolean isStockSymbolValid = marketDataService.isValidStockSymbol(stockSymbol);
        if (isStockSymbolValid) {
            PortfolioTransaction portfolioTransaction = portfolioTransactionMapper.toEntity(userId, portfolioTransactionRequest);
            PortfolioTransaction savedTransaction  = portfolioTransactionRepository.save(portfolioTransaction);
            return portfolioTransactionMapper.toResponseDto(savedTransaction);
        } else {
            throw new InvalidSymbolsException(stockSymbol);
        }

    }

    @Override
    public Page<PortfolioTransactionResponse> getPortfolioTransactionsByUserId(String userId, int page, int size, Sort sort) {


        if (page < 0) {
            List<PortfolioTransaction> allTransactions  = portfolioTransactionRepository.findByUserId(userId);

            if (allTransactions.isEmpty()) {
                throw new ResourceNotFoundException("Transactions", "userId", userId);
            }

            List<PortfolioTransactionResponse> responseList = allTransactions.stream()
                    .map(portfolioTransactionMapper::toResponseDto)
                    .collect(Collectors.toList());

            return new PageImpl<>(responseList, Pageable.unpaged(), responseList.size());

        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PortfolioTransaction> portfolioTransactions = portfolioTransactionRepository.findByUserId(userId, pageable);

        if (portfolioTransactions.isEmpty()) {
            throw new ResourceNotFoundException("Transactions", "userId", userId);
        }

        List<PortfolioTransactionResponse> responseList = portfolioTransactions.stream()
                .map(portfolioTransactionMapper::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, portfolioTransactions.getTotalElements());

    }

    @Transactional
    @Override
    public void deletePortfolioTransaction(String userId, long transactionId) {

        if (!portfolioTransactionRepository.existsByUserIdAndId(userId, transactionId)) {
            throw new ResourceNotFoundException("Transaction", "userId/transactionId", userId + "/" + transactionId);
        }

        portfolioTransactionRepository.deleteByUserIdAndId(userId, transactionId);
        boolean existsAfterDelete = portfolioTransactionRepository.existsByUserIdAndId(userId, transactionId);
        if (existsAfterDelete){
            throw new ResourceNotDeletedException("Transaction", "userId/transactionId", userId + "/" + Long.toString(transactionId));
        }

    }

    @Transactional
    @Override
    public void deleteAllTransactionsByUserIdAndStockSymbol(String userId, String stockSymbol) {
        if (!portfolioTransactionRepository.existsByUserIdAndStockSymbol(userId, stockSymbol)) {
            throw new ResourceNotFoundException("Stock Symbol", "userId/stockSymbol", userId + "/" + stockSymbol );
        }
        portfolioTransactionRepository.deleteByUserIdAndStockSymbol(userId, stockSymbol);
        boolean existsAfterDelete = portfolioTransactionRepository.existsByUserIdAndStockSymbol(userId, stockSymbol);
        if (existsAfterDelete){
            throw new ResourceNotDeletedException("Stock Symbol", "userId/stockSymbol", userId + "/" + stockSymbol);
        }
    }

    @Override
    public List<PortfolioStockResponse> getUserPortfolioStocks(String userId, Pageable pageable) {

        List<PortfolioStockResponse> response = new ArrayList<>();
        Map<String, StockMarketDataResponse> userStocksMarketData;

        List<PortfolioStock> userPortfolioStocks =  portfolioTransactionRepository.getUserPortfolioStocks(userId, pageable);

        if(userPortfolioStocks.isEmpty()){
            throw new ResourceNotFoundException("Stocks","userId",userId);
        }

        Set<String> userStocksSymbols = userPortfolioStocks.stream()
                .map(PortfolioStock::getStockSymbol)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        userStocksMarketData = marketDataService.fetchStocksMarketData(new ArrayList<>(userStocksSymbols));

        for ( String key : userStocksMarketData.keySet() ) {

            BigDecimal averageCost = BigDecimal.valueOf(
                    userPortfolioStocks
                            .stream()
                            .filter(s -> key.equals(s.getStockSymbol()))
                            .mapToDouble(PortfolioStock::getQuantity)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Average cost not found for stock: " + key))
            );

            BigDecimal quantity = userPortfolioStocks
                    .stream()
                    .filter( s -> key.equals(s.getStockSymbol()))
                    .map(s -> BigDecimal.valueOf(s.getQuantity()))
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            BigDecimal latestPrice = userStocksMarketData.get(key).getLatestPrice();

            BigDecimal profitLossPct = latestPrice
                    .divide(averageCost, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal profitLossInTL = latestPrice.subtract(averageCost).multiply(quantity);

            response.add(
                    PortfolioStockResponse
                            .builder()
                            .stockSymbol(key)
                            .latestPrice(userStocksMarketData.get(key).getLatestPrice())
                            .dailyChangePct(userStocksMarketData.get(key).getDailyChangePct())
                            .dailyChangeInTL(userStocksMarketData.get(key).getDailyChangeInTL())
                            .averageCost(averageCost)
                            .profitLossPct(profitLossPct)
                            .profitLossInTL(profitLossInTL)
                            .quantity(quantity)
                            .build()
            );
        }

        return response;
    }
}
