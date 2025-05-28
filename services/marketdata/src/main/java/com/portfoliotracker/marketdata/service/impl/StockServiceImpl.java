package com.portfoliotracker.marketdata.service.impl;

import com.portfoliotracker.marketdata.exception.InvalidSymbolsException;
import com.portfoliotracker.marketdata.exception.NoMarketDataFoundException;
import com.portfoliotracker.marketdata.dto.StockResponse;
import com.portfoliotracker.marketdata.service.StockService;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private Environment env;

    private Document document;

    @Value(("${STOCK_DATA_URL}"))
    private String stockDataUrl;

    private Map<String, StockResponse> stocksMarketData = new ConcurrentHashMap<>();

    private static final Logger logger = LogManager.getLogger(StockServiceImpl.class);

    @Override
    public List<String> getAllStockSymbols() {
        if(stocksMarketData.keySet().isEmpty()){
            throw new NoMarketDataFoundException();
        }
        return new ArrayList<>(stocksMarketData.keySet());
    }

    @Override
    public Map<String, StockResponse>  getStocksMarketData(List<String> stockSymbols) {
        Map<String, StockResponse>  response = new LinkedHashMap<>();

        if(stocksMarketData.keySet().isEmpty()){
            throw new NoMarketDataFoundException();
        }

        for (String symbol : stockSymbols){
            if (stocksMarketData.containsKey(symbol)) {
                response.put(symbol,stocksMarketData.get(symbol));
            }
        }

        if(response.isEmpty()){
            String givenStockSymbols = String.join(",", stockSymbols);
            throw new InvalidSymbolsException(givenStockSymbols);
        }

        return response;
    }

    @PostConstruct
    @Scheduled(cron = "0 */3 8-21 * * *", zone = "Europe/Istanbul")
    private void updateStockData() {

        if (stockDataUrl == null || stockDataUrl.trim().isEmpty()) {
            logger.error("STOCK_DATA_URL for stock market data is null or empty. " +
                    "Failed to fetch stock data from {}", stockDataUrl
            );

            return;
        }

        try {
        logger.info("Fetching stock data from {}", stockDataUrl);
        document = Jsoup.connect(Objects.requireNonNull(stockDataUrl)).get();
        logger.info("Successfully fetched stock data from {} ", stockDataUrl);
        updateStocksMarketData();
        } catch (Exception e) {
            logger.error("Failed to fetch stock data from {}. Error: {}", stockDataUrl, e.getMessage(), e);
        }
    }

    private void  updateStocksMarketData(){
        Map<String, StockResponse> updatedStocksMarketData = new HashMap<>();

        if (document == null){
            logger.error("Document is null. Failed to update stocksMarketData. " +
                    "Check if url fetched properly or not from {}"
                    , stockDataUrl
            );
            return;
        }

        Elements rows = document.select("table.dataTable tbody tr");
        if (rows.isEmpty()) {
            logger.warn("No stock data rows found in the table for URL: {}", stockDataUrl);
            return;
        }

        for(Element row : rows){
            try{
                String stockCode = row.selectFirst("td a").text().trim();
                BigDecimal latestPrice = parseBigDecimal(row.select("td.text-right").get(0).text());
                BigDecimal dailyChangePct = parseBigDecimal(row.select("td.text-right").get(1).text());
                BigDecimal dailyChangeInTL = parseBigDecimal(row.select("td.text-right").get(2).text());
                BigDecimal tradingVolumeTL = parseBigDecimal(row.select("td.text-right").get(3).text());
                BigDecimal tradeVolumeCount = parseBigDecimal(row.select("td.text-right").get(4).text());
                StockResponse stock = StockResponse.builder()
                        .stockSymbol(stockCode)
                        .latestPrice(latestPrice)
                        .dailyChangePct(dailyChangePct)
                        .dailyChangeInTL(dailyChangeInTL)
                        .tradingVolumeTL(tradingVolumeTL)
                        .tradeVolumeCount(tradeVolumeCount)
                        .build();
                updatedStocksMarketData.put(stockCode,stock);
            }catch (Exception e){
                logger.error("Error processing row: {}", row.toString(), e);
            }
        }
        // Corrupted market data can be retrived just before openning market.
        // The section check if it is corrupted.
        long possibleCorruptedDataCount = updatedStocksMarketData
                .values()
                .stream()
                .filter(
                        stock -> stock.getDailyChangePct().compareTo(new BigDecimal("0.43"))  == 0
                )
                .count();
        Boolean isMarketDataInvalid = (((double) possibleCorruptedDataCount / updatedStocksMarketData.keySet().size()) * 100) > 20;

        if (!isMarketDataInvalid) {
            stocksMarketData = updatedStocksMarketData;
        }


    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Value is empty or null for BigDecimal parsing");
        }
        try {
            return new BigDecimal(value.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse BigDecimal from value: " + value, e);
        }
    }
}
