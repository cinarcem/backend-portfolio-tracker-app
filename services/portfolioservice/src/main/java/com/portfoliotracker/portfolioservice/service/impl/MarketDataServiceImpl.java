package com.portfoliotracker.portfolioservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfoliotracker.portfolioservice.common.ApiCustomResponse;
import com.portfoliotracker.portfolioservice.dto.response.StockMarketDataResponse;
import com.portfoliotracker.portfolioservice.service.MarketDataService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;


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

    @Override
    public boolean isValidStockSymbol(String symbol) {

        try {

            ApiCustomResponse<List<String>> serviceResponse = webClient.get()
                    .uri("/market-data/api/v1/stocks/symbols")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiCustomResponse<List<String>>>() {})
                    .block();

            List<String> stockSymbols = serviceResponse != null ? serviceResponse.getData() : Collections.emptyList();

            return stockSymbols.contains(symbol);

        }  catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while validation stock symbol.", e);
        }

    }

    @Override
    public Map<String, StockMarketDataResponse> fetchStocksMarketData(List<String> symbols) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, StockMarketDataResponse> response = new LinkedHashMap<>();

        try {

            String uri = UriComponentsBuilder.fromUriString("/market-data/v1/stocks")
                    .queryParam("symbols", symbols != null && !symbols.isEmpty() ? symbols.toArray(new String[0]) : null)
                    .build()
                    .toString();

            ApiCustomResponse<List<StockMarketDataResponse>> serviceResponse = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiCustomResponse<List<StockMarketDataResponse>>>() {})
                    .block();

            for (StockMarketDataResponse userStock : serviceResponse.getData()){
                response.put(userStock.getStockSymbol(),userStock);
            }

            return response;
        } catch (WebClientResponseException e) {
            throw new RuntimeException("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching stock details", e);
        }
    }

}
