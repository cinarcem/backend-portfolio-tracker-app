package com.portfoliotracker.portfolioservice.service;

import com.portfoliotracker.portfolioservice.dto.request.PortfolioTransactionRequest;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioStockResponse;
import com.portfoliotracker.portfolioservice.dto.response.PortfolioTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PortfolioService {

    PortfolioTransactionResponse savePortfolioTransaction(String userId, PortfolioTransactionRequest portfolioTransactionRequest);
    List<PortfolioTransactionResponse> getAllPortfolioTransactionsByUserId(String userId);
    List<PortfolioTransactionResponse> getPortfolioTransactionsByUserId(String userId, Pageable pageable);
    void deletePortfolioTransaction(String userId, long transactionId);
    void deleteAllTransactionsByUserIdAndStockSymbol(String userId, String stockSymbol);

    List<PortfolioStockResponse> getUserPortfolioStocks(String userId, Pageable pageable);
}
