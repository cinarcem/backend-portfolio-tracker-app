package com.portfoliotracker.portfolioservice.service.impl;

import com.portfoliotracker.portfolioservice.dto.request.PortfolioTransactionRequest;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioStockResponse;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioTransactionResponse;
import com.portfoliotracker.portfolioservice.dto.response.StockMarketDataResponse;
import com.portfoliotracker.portfolioservice.entity.PortfolioTransaction;
import com.portfoliotracker.portfolioservice.exception.InvalidSymbolsException;
import com.portfoliotracker.portfolioservice.exception.ResourceNotFoundException;
import com.portfoliotracker.portfolioservice.exception.ResourceNotDeletedException;
import com.portfoliotracker.portfolioservice.exception.UnknownSortPropertyException;
import com.portfoliotracker.portfolioservice.mapper.PortfolioTransactionMapper;
import com.portfoliotracker.portfolioservice.projection.PortfolioStock;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioTransactionRepository portfolioTransactionRepository;
    private final MarketDataService marketDataService;
    private final PortfolioTransactionMapper portfolioTransactionMapper;

    /**
     * Saves a portfolio transaction for a specific user.
     *
     * @param userId                      the ID of the user performing the transaction.
     * @param portfolioTransactionRequest the transaction details including stock symbol, quantity, etc.
     * @return a response DTO containing details of the saved transaction.
     * @throws InvalidSymbolsException if the provided stock symbol is invalid.
     */
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

    /**
     * Retrieves a paginated list of portfolio transactions for a specific user.
     *
     * @param userId the ID of the user whose transactions are being fetched.
     * @param page   the page number to retrieve (use -1 to retrieve all transactions without pagination).
     * @param size   the number of transactions per page.
     * @param sort   the sorting criteria for transactions.
     * @return a paginated list of portfolio transaction responses.
     * @throws ResourceNotFoundException if no transactions are found for the given user ID.
     */
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

    /**
     * Deletes a specific portfolio transaction for a user.
     *
     * @param userId        the ID of the user.
     * @param transactionId the ID of the transaction to delete.
     * @throws ResourceNotFoundException   if the transaction does not exist for the given user ID and transaction ID.
     * @throws ResourceNotDeletedException if the transaction was not successfully deleted.
     */
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

    /**
     * Deletes all portfolio transactions for a specific stock symbol and user.
     *
     * @param userId     the ID of the user.
     * @param stockSymbol the stock symbol for which transactions are to be deleted.
     * @throws ResourceNotFoundException   if no transactions are found for the given stock symbol and user ID.
     * @throws ResourceNotDeletedException if the transactions were not successfully deleted.
     */
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

    /**
     * Retrieves a paginated list of portfolio stocks for a user, enriched with market data.
     *
     * @param userId the ID of the user.
     * @param page   the page number to retrieve (use -1 to retrieve all stocks without pagination).
     * @param size   the number of stocks per page.
     * @param sort   the sorting criteria for stocks.
     * @return a paginated list of portfolio stocks, including market data such as latest price and profit/loss.
     * @throws ResourceNotFoundException if no stocks are found for the given user ID.
     * @throws IllegalArgumentException  if stock data is missing for any symbol.
     */
    @Override
    public Page<PortfolioStockResponse> getUserPortfolioStocks(String userId, int page, int size, Sort sort) {

        List<PortfolioStock> userPortfolioStocks =  portfolioTransactionRepository.getUserPortfolioStocks(userId);

        if(userPortfolioStocks.isEmpty()){
            throw new ResourceNotFoundException("Stocks","userId",userId);
        }

        Map<String, PortfolioStock> mappedUserPortfolioStocks = userPortfolioStocks.stream()
                .collect(Collectors.toMap(
                        PortfolioStock::getStockSymbol,
                        Function.identity(),
                        (existing, replacement) -> existing));

        Map<String, StockMarketDataResponse> userStocksMarketData = marketDataService
                .fetchStocksMarketData(new ArrayList<>(mappedUserPortfolioStocks.keySet()));

        List<PortfolioStockResponse> stocksWithMarketData = new ArrayList<>();
        for (Map.Entry<String, StockMarketDataResponse> stringStockMarketDataResponseEntry : userStocksMarketData.entrySet()) {
            String symbol = stringStockMarketDataResponseEntry.getKey();
            StockMarketDataResponse marketData = stringStockMarketDataResponseEntry.getValue();
            PortfolioStock portfolioStock = mappedUserPortfolioStocks.get(symbol);

            if (portfolioStock == null) {
                throw new IllegalArgumentException("Stock data not found for symbol: " + symbol);
            }

            BigDecimal averageCost = BigDecimal.valueOf(portfolioStock.getAverageCost());
            BigDecimal quantity = BigDecimal.valueOf(portfolioStock.getQuantity());
            BigDecimal latestPrice = marketData.getLatestPrice();

            BigDecimal profitLossPct = latestPrice
                    .divide(averageCost, 4, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal profitLossInTL = latestPrice.subtract(averageCost).multiply(quantity);

            PortfolioStockResponse portfolioStockResponseDto = PortfolioStockResponse.builder()
                    .stockSymbol(symbol)
                    .latestPrice(latestPrice)
                    .dailyChangePct(marketData.getDailyChangePct())
                    .dailyChangeInTL(marketData.getDailyChangeInTL())
                    .averageCost(averageCost)
                    .profitLossPct(profitLossPct)
                    .profitLossInTL(profitLossInTL)
                    .quantity(quantity)
                    .build();
            stocksWithMarketData.add(portfolioStockResponseDto);
        }

        stocksWithMarketData = sortPortfolioStockResponses(stocksWithMarketData, sort);

        if (page < 0) {
            return new PageImpl<>(
                    stocksWithMarketData,
                    PageRequest.of(0, Math.max(stocksWithMarketData.size(), 1), sort),
                    stocksWithMarketData.size()
            );
        }

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        int totalElements = stocksWithMarketData.size();

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), totalElements);

        if (start > totalElements) {
            return new PageImpl<>(new ArrayList<>(), pageRequest, totalElements);
        }

        List<PortfolioStockResponse> pageContent = stocksWithMarketData.subList(start, end);

        return new PageImpl<>(pageContent, pageRequest, totalElements);

    }

    /**
     * Helper method to sort a list of portfolio stock responses based on the provided sorting criteria.
     *
     * @param list the list of portfolio stock responses to sort.
     * @param sort the sorting criteria.
     * @return a sorted list of portfolio stock responses.
     */
    private List<PortfolioStockResponse> sortPortfolioStockResponses(List<PortfolioStockResponse> list, Sort sort) {

        if (sort.isUnsorted()) {
            return list;
        }

        return list.stream().sorted((o1, o2) -> {
            for (Sort.Order order : sort) {
                Comparator<PortfolioStockResponse> comparator = getComparator(order);
                int result = comparator.compare(o1, o2);
                if (result != 0) {
                    return order.isAscending() ? result : -result;
                }
            }
            return 0;
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves a comparator for sorting portfolio stock responses based on a specific property.
     *
     * @param order the sorting order containing the property and direction.
     * @return a comparator for the specified property.
     * @throws UnknownSortPropertyException if the property is not recognized.
     */
    private Comparator<PortfolioStockResponse> getComparator(Sort.Order order) {
        return switch (order.getProperty()) {
            case "stockSymbol" -> Comparator.comparing(PortfolioStockResponse::getStockSymbol);
            case "latestPrice" -> Comparator.comparing(PortfolioStockResponse::getLatestPrice);
            case "dailyChangePct" -> Comparator.comparing(PortfolioStockResponse::getDailyChangePct);
            case "averageCost" -> Comparator.comparing(PortfolioStockResponse::getAverageCost);
            case "profitLossPct" -> Comparator.comparing(PortfolioStockResponse::getProfitLossPct);
            case "profitLossInTL" -> Comparator.comparing(PortfolioStockResponse::getProfitLossInTL);
            case "quantity" -> Comparator.comparing(PortfolioStockResponse::getQuantity);
            default -> throw new UnknownSortPropertyException( order.getProperty());
        };
    }
}
