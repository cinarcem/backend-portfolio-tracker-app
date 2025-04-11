package com.portfoliotracker.watchlistservice.repository;

import com.portfoliotracker.watchlistservice.entity.StocksWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StocksWatchlistRepository extends JpaRepository<StocksWatchlist,Long> {

    List<StocksWatchlist> findByUserId(String userId);
    void deleteByUserIdAndStockSymbol(String userId, String stockSymbol);

    @Query("SELECT sw.stockSymbol FROM StocksWatchlist sw WHERE sw.userId = :userId")
    List<String> findStockSymbolsByUserId(String userId);

}
