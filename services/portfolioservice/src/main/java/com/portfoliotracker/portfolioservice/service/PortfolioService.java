package com.portfoliotracker.portfolioservice.service;

import com.portfoliotracker.portfolioservice.dto.request.PortfolioTransactionRequest;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioStockResponse;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface PortfolioService {

    PortfolioTransactionResponse savePortfolioTransaction(String userId, PortfolioTransactionRequest portfolioTransactionRequest);
    Page<PortfolioTransactionResponse> getPortfolioTransactionsByUserId(String userId, int page, int size, Sort sort);
    void deletePortfolioTransaction(String userId, long transactionId);
    void deleteAllTransactionsByUserIdAndStockSymbol(String userId, String stockSymbol);

    Page<PortfolioStockResponse> getUserPortfolioStocks(String userId, int page, int size, Sort sort);
}
