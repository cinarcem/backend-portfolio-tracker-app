package com.portfoliotracker.marketdata.service;

import com.portfoliotracker.marketdata.model.Index;

import java.util.List;
import java.util.Map;

public interface IndexService {

    Map<String, String> getAllIndexSymbolsAndNames();
    Map<String, Index> getIndexesMarketData(List<String> symbols);

}
