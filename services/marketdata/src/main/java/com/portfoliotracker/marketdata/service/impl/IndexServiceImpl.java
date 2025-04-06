package com.portfoliotracker.marketdata.service.impl;

import com.portfoliotracker.marketdata.exception.InvalidSymbolsException;
import com.portfoliotracker.marketdata.exception.NoMarketDataFoundException;
import com.portfoliotracker.marketdata.dto.IndexResponse;
import com.portfoliotracker.marketdata.service.IndexService;
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
public class IndexServiceImpl implements IndexService {

    @Autowired
    private Environment env;

    private Document document;

    @Value("${INDEX_DATA_URL}")
    private String indexDataUrl;

    private Map<String, IndexResponse> indexesMarketData = new ConcurrentHashMap<>();

    private static final Logger logger = LogManager.getLogger(IndexServiceImpl.class);

    @Override
    public Map<String, String> getAllIndexSymbolsAndNames() {

        Map<String, String> allIndexSymbolsAndNames = new HashMap<>();

        if(indexesMarketData.keySet().isEmpty()){
            throw new NoMarketDataFoundException();
        }

        for (String symbol : indexesMarketData.keySet()){
            allIndexSymbolsAndNames.put(symbol, indexesMarketData.get(symbol).getName());
        }

        return allIndexSymbolsAndNames;

    }

    @Override
    public Map<String, IndexResponse> getIndexesMarketData(List<String> symbols) {
        Map<String, IndexResponse>  response = new HashMap<>();

        if(indexesMarketData.keySet().isEmpty()){
            throw new NoMarketDataFoundException();
        }

        for (String symbol : symbols){
            if (indexesMarketData.containsKey(symbol)) {
                response.put(symbol,indexesMarketData.get(symbol));
            }
        }

        if(response.isEmpty()){
            String givenStockSymbols = String.join(",", symbols);
            throw new InvalidSymbolsException(givenStockSymbols);
        }

        return response;
    }

    @PostConstruct
    @Scheduled(cron = "0 */3 8-21 * * *", zone = "Europe/Istanbul")
    private void updateIndexData() {

        if (indexDataUrl == null || indexDataUrl.trim().isEmpty()) {
            logger.error("INDEX_DATA_URL for indexes market data is null or empty. " +
                    "Failed to fetch stock data from {}", indexDataUrl
            );

            return;
        }

        try {
            logger.info("Fetching indexes data from {}", indexDataUrl);
            document = Jsoup.connect(Objects.requireNonNull(indexDataUrl)).get();
            logger.info("Successfully fetched indexes data from {} ", indexDataUrl);
            updateIndexesMarketData();
        } catch (Exception e) {
            logger.error("Failed to fetch indexes data from {}. Error: {}", indexDataUrl, e.getMessage(), e);
        }
    }

    private void  updateIndexesMarketData(){

        Map<String, IndexResponse> updatedIndexesMarketData = new HashMap<>();

        if (document == null){
            logger.error("Document is null. Failed to update indexesMarketData. " +
                            "Check if url fetched properly or not from {}"
                    , indexDataUrl
            );
            return;
        }

        Elements rows = document.select("table.dataTable tbody tr");
        if (rows.isEmpty()) {
            logger.warn("No index data rows found in the table for URL: {}", indexDataUrl);
            return;
        }

        for(Element row : rows){
            try {
                String symbol = row.selectFirst("td[title]").attr("title") ;
                String name =row.select("td").get(0).text();
                BigDecimal latestValue = parseBigDecimal(row.select("td").get(1).text());
                BigDecimal dailyChangePct = parseBigDecimal(row.select("td").get(2).text());
                BigDecimal  weeklyChangePct = parseBigDecimal(row.select("td").get(3).text());
                BigDecimal  monthlyChangePct = parseBigDecimal(row.select("td").get(4).text());
                BigDecimal  yearlyChangePct = parseBigDecimal(row.select("td").get(5).text());

                IndexResponse index = IndexResponse.builder()
                        .symbol(symbol)
                        .name(name)
                        .latestValue(latestValue)
                        .dailyChangePct(dailyChangePct)
                        .weeklyChangePct(weeklyChangePct)
                        .monthlyChangePct(monthlyChangePct)
                        .yearlyChangePct(yearlyChangePct)
                        .build();

                updatedIndexesMarketData.put(symbol, index);

            }catch (Exception e){
                logger.error("Error processing row: {}", row.toString(), e);
            }

            indexesMarketData = updatedIndexesMarketData;

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
