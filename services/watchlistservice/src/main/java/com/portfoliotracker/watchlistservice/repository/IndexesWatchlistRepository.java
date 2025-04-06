package com.portfoliotracker.watchlistservice.repository;

import com.portfoliotracker.watchlistservice.entity.IndexesWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexesWatchlistRepository extends JpaRepository<IndexesWatchlist,Long> {

    List<IndexesWatchlist> findByUserId(String userId);
    boolean existsByUserIdAndIndexSymbol(String userId, String indexSymbol);
    void deleteByUserIdAndIndexSymbol(String userId, String indexSymbol);

    @Query("SELECT iw.indexSymbol FROM IndexesWatchlist iw WHERE iw.userId = :userId")
    List<String> findIndexSymbolsByUserId(String userId);

}