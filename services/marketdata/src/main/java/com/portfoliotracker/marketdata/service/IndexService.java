package com.portfoliotracker.marketdata.service;

import com.portfoliotracker.marketdata.dto.IndexResponse;

import java.util.List;
import java.util.Map;

public interface IndexService {

    Map<String, String> getAllIndexSymbolsAndNames();
    Map<String, IndexResponse> getIndexesMarketData(List<String> symbols);

}
