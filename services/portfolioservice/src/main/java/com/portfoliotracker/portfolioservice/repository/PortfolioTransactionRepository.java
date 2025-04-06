package com.portfoliotracker.portfolioservice.repository;

import com.portfoliotracker.portfolioservice.entity.PortfolioTransaction;
import com.portfoliotracker.portfolioservice.projection.PortfolioStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioTransactionRepository extends JpaRepository<PortfolioTransaction, Long> {
    List<PortfolioTransaction> findByUserId(String userId);
    Page<PortfolioTransaction> findByUserId(String userId, Pageable pageable);
    boolean existsByUserIdAndId(String userId, long id);
    void deleteByUserIdAndId(String userId, long id);
    void deleteByUserIdAndStockSymbol(String userId, String stockSymbol);
    boolean existsByUserIdAndStockSymbol(String userId, String stockSymbol);

    @Query("SELECT new com.portfoliotracker.portfolioservice.projection.PortfolioStock(" +
            "t.stockSymbol, " +
            "ROUND((SUM(t.price * t.quantity) / SUM(t.quantity)) * 1.0, 2) AS averageCost, " +
            "SUM(t.quantity) * 1.0 AS quantity)" +
            "FROM PortfolioTransaction t " +
            "WHERE t.userId = :userId " +
            "GROUP BY t.stockSymbol")
    List<PortfolioStock> getUserPortfolioStocks(String userId);

}
