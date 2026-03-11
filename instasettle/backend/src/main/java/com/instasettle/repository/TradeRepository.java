package com.instasettle.repository;

import com.instasettle.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByBuyerIdOrSellerId(Long buyerId, Long sellerId);
    List<Trade> findByStatus(String status);
}
