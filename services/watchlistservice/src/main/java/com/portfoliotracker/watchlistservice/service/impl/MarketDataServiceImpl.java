package com.portfoliotracker.watchlistservice.service.impl;

import com.portfoliotracker.watchlistservice.common.ApiCustomResponse;
import com.portfoliotracker.watchlistservice.dto.response.MarketDataServiceIndexResponse;
import com.portfoliotracker.watchlistservice.dto.response.IndexWithMarketDataResponse;
import com.portfoliotracker.watchlistservice.dto.response.MarketDataServiceStockResponse;
import com.portfoliotracker.watchlistservice.dto.response.StockWithMarketDataResponse;
import com.portfoliotracker.watchlistservice.exception.UnknownSortPropertyException;
import com.portfoliotracker.watchlistservice.service.MarketDataService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation that communicates with the external Market Data service
 * to fetch stock and index-related data using WebClient.
 */
@Service
public class MarketDataServiceImpl implements MarketDataService {

    private final WebClient webClient;

    public MarketDataServiceImpl(WebClient.Builder webClientBuilder , Environment env) {

        String baseUrl = env.getProperty("MARKET_DATA_SERVICE_BASE_URL");

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Market data service base URL must not be null or empty");
        }

        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Fetches all index symbols from the market data service.
     *
     * @return a set of all index symbols
     */
    @Override
    public Set<String> getAllIndexSymbols() {

        try {

            String uri = UriComponentsBuilder.fromUriString("/market-data/api/v1/indexes/symbols")
                    .build()
                    .toString();

            ApiCustomResponse<Map<String, String>> serviceResponse = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiCustomResponse<Map<String, String>>>() {})
                    .block();

            Set<String> allIndexSymbols = serviceResponse != null ? serviceResponse.getData().keySet() : Collections.emptySet();

            return allIndexSymbols;

        } catch (WebClientResponseException e) {
            throw new RuntimeException("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching all index symbols.", e);
        }

    }


    /**
     * Fetches all stock symbols from the market data service.
     *
     * @return a list of all stock symbols
     */
    @Override
    public List<String> getAllStockSymbols() {

        try {

            String uri = UriComponentsBuilder.fromUriString("/market-data/api/v1/stocks/symbols")
                    .build()
                    .toString();

            ApiCustomResponse<List<String>> serviceResponse = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiCustomResponse<List<String>>>() {})
                    .block();

            List<String> allStockSymbols = serviceResponse != null ? serviceResponse.getData() : Collections.emptyList();

            return allStockSymbols;

        } catch (WebClientResponseException e) {
            throw new RuntimeException("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching all stock symbols.", e);
        }

    }

    /**
     * Fetches stock market data for a list of symbols, applies sorting and pagination.
     *
     * @param stockSymbols list of stock symbols to retrieve data for
     * @param page page number
     * @param size page size
     * @param sort sorting criteria
     * @return a Page object containing sorted and paginated stock data
     */
    @Override
    public Page<StockWithMarketDataResponse> fetchStocksMarketData(List<String> stockSymbols, int page, int size, Sort sort) {

        List<StockWithMarketDataResponse> stockWithMarketDataResponses = new ArrayList<>();

        try {
            String uri = UriComponentsBuilder.fromUriString("/market-data/api/v1/stocks")
                    .queryParam("symbols", String.join(",", stockSymbols))
                    .build()
                    .toString();

            ApiCustomResponse<List<MarketDataServiceStockResponse>> serviceResponse = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiCustomResponse<List<MarketDataServiceStockResponse>>>() {})
                    .block();

            // Map each symbol to its response or create an empty response if not found
            for (String stockSymbol : stockSymbols) {

                StockWithMarketDataResponse stockWithMarketDataResponse;

                Optional<MarketDataServiceStockResponse> marketDataIndexResponseOptional = serviceResponse.getData().stream()
                        .filter(s -> s.getStockSymbol().equals(stockSymbol))
                        .findFirst();
                if (marketDataIndexResponseOptional.isPresent()) {

                    MarketDataServiceStockResponse marketDataServiceStockResponse = marketDataIndexResponseOptional.get();
                    stockWithMarketDataResponse = StockWithMarketDataResponse.builder()
                            .stockSymbol(stockSymbol)
                            .latestValue(marketDataServiceStockResponse.getLatestPrice())
                            .dailyChangePct(marketDataServiceStockResponse.getDailyChangePct())
                            .build();

                } else {
                    stockWithMarketDataResponse = StockWithMarketDataResponse.builder()
                            .stockSymbol(stockSymbol)
                            .latestValue(null)
                            .dailyChangePct(null)
                            .build();
                }
                stockWithMarketDataResponses.add(stockWithMarketDataResponse);
            }
        } catch (WebClientResponseException e) {
            throw new RuntimeException(String.format("HTTP error while fetching stocks market data... Status Code: '%s'." +
                    "Response Body: '%s'.",e.getStatusCode(),e.getResponseBodyAsString()));
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching market data for given stock symbols. " +
                    "Exception message: %s" + e.getMessage());
        }

        // Handle pagination
        stockWithMarketDataResponses = sortStockWithMarketDataResponse(stockWithMarketDataResponses, sort);

        // Handle pagination
        if (page < 0) {
            return new PageImpl<>(
                    stockWithMarketDataResponses,
                    PageRequest.of(0, Math.max(stockWithMarketDataResponses.size(), 1), sort),
                    stockWithMarketDataResponses.size()
            );
        }

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        int totalElements = stockWithMarketDataResponses.size();

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), totalElements);

        if (start > totalElements) {
            return new PageImpl<>(new ArrayList<>(), pageRequest, totalElements);
        }

        List<StockWithMarketDataResponse> pageContent = stockWithMarketDataResponses.subList(start, end);

        return new PageImpl<>(pageContent, pageRequest, totalElements);

    }

    /**
     * Fetches index market data for a list of index symbols, applies sorting and pagination.
     *
     * @param indexSymbols list of index symbols to retrieve data for
     * @param page page number (zero-based)
     * @param size page size
     * @param sort sorting criteria
     * @return a Page object containing sorted and paginated index data
     */
    @Override
    public Page<IndexWithMarketDataResponse> fetchIndexesMarketData(List<String> indexSymbols, int page, int size, Sort sort) {
        try {
            List<IndexWithMarketDataResponse> indexWithMarketDataResponses = new ArrayList<>();

            String uri = UriComponentsBuilder.fromUriString("/market-data/api/v1/indexes")
                    .queryParam("symbols", String.join(",", indexSymbols))
                    .build()
                    .toString();

            ApiCustomResponse<List<MarketDataServiceIndexResponse>> serviceResponse = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiCustomResponse<List<MarketDataServiceIndexResponse>>>() {})
                    .block();

            for (String indexSymbol : indexSymbols) {
                IndexWithMarketDataResponse indexWithMarketDataResponse;
                Optional<MarketDataServiceIndexResponse> marketDataIndexResponseOptional = serviceResponse.getData().stream()
                        .filter(s -> s.getSymbol().equals(indexSymbol))
                        .findFirst();
                if (marketDataIndexResponseOptional.isPresent()) {
                    MarketDataServiceIndexResponse marketDataServiceIndexResponse = marketDataIndexResponseOptional.get();
                    indexWithMarketDataResponse = IndexWithMarketDataResponse.builder()
                            .indexSymbol(indexSymbol)
                            .latestValue(marketDataServiceIndexResponse.getLatestValue())
                            .dailyChangePct(marketDataServiceIndexResponse.getDailyChangePct())
                            .build();
                } else {
                    indexWithMarketDataResponse = IndexWithMarketDataResponse.builder()
                            .indexSymbol(indexSymbol)
                            .latestValue(null)
                            .dailyChangePct(null)
                            .build();
                }
                indexWithMarketDataResponses.add(indexWithMarketDataResponse);
            }

            // Handle pagination
            indexWithMarketDataResponses = sortIndexWithMarketDataResponse(indexWithMarketDataResponses, sort);

            // Handle pagination
            if (page < 0) {
                return new PageImpl<>(
                        indexWithMarketDataResponses,
                        PageRequest.of(0, Math.max(indexWithMarketDataResponses.size(), 1), sort),
                        indexWithMarketDataResponses.size()
                );
            }

            PageRequest pageRequest = PageRequest.of(page, size, sort);

            int totalElements = indexWithMarketDataResponses.size();

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), totalElements);

            if (start > totalElements) {
                return new PageImpl<>(new ArrayList<>(), pageRequest, totalElements);
            }

            List<IndexWithMarketDataResponse> pageContent = indexWithMarketDataResponses.subList(start, end);

            return new PageImpl<>(pageContent, pageRequest, totalElements);

        } catch (WebClientResponseException e) {
            throw new RuntimeException(String.format("HTTP error while fetching indexes market data... Status Code: '%s'." +
                    "Response Body: '%s'.",e.getStatusCode(),e.getResponseBodyAsString()));
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching market data for given index symbols. " +
                    "Exception message: %s" + e.getMessage());
        }
    }

    private List<StockWithMarketDataResponse> sortStockWithMarketDataResponse(List<StockWithMarketDataResponse> list, Sort sort) {

        if (sort.isUnsorted()) {
            return list;
        }

        return list.stream().sorted((o1, o2) -> {
            for (Sort.Order order : sort) {
                Comparator<StockWithMarketDataResponse> comparator = getStockComparator(order);
                int result = comparator.compare(o1, o2);
                if (result != 0) {
                    return order.isAscending() ? result : -result;
                }
            }
            return 0;
        }).collect(Collectors.toList());
    }

    private Comparator<StockWithMarketDataResponse> getStockComparator(Sort.Order order) {
        return switch (order.getProperty()) {
            case "stockSymbol" -> Comparator.comparing(StockWithMarketDataResponse::getStockSymbol);
            case "latestValue" -> Comparator.comparing(StockWithMarketDataResponse::getLatestValue);
            case "dailyChangePct" -> Comparator.comparing(StockWithMarketDataResponse::getDailyChangePct);
            default -> throw new UnknownSortPropertyException( order.getProperty());
        };
    }


    private List<IndexWithMarketDataResponse> sortIndexWithMarketDataResponse(List<IndexWithMarketDataResponse> list, Sort sort) {

        if (sort.isUnsorted()) {
            return list;
        }

        return list.stream().sorted((o1, o2) -> {
            for (Sort.Order order : sort) {
                Comparator<IndexWithMarketDataResponse> comparator = getIndexComparator(order);
                int result = comparator.compare(o1, o2);
                if (result != 0) {
                    return order.isAscending() ? result : -result;
                }
            }
            return 0;
        }).collect(Collectors.toList());
    }

    private Comparator<IndexWithMarketDataResponse> getIndexComparator(Sort.Order order) {
        return switch (order.getProperty()) {
            case "indexSymbol" -> Comparator.comparing(IndexWithMarketDataResponse::getIndexSymbol);
            case "latestValue" -> Comparator.comparing(IndexWithMarketDataResponse::getLatestValue);
            case "dailyChangePct" -> Comparator.comparing(IndexWithMarketDataResponse::getDailyChangePct);
            default -> throw new UnknownSortPropertyException( order.getProperty());
        };
    }


}
