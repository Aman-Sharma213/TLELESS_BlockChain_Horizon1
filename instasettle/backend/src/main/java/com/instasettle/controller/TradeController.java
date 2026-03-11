package com.instasettle.controller;

import com.instasettle.model.Settlement;
import com.instasettle.model.Trade;
import com.instasettle.model.User;
import com.instasettle.repository.TradeRepository;
import com.instasettle.service.TradeBookService;
import com.instasettle.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final TradeBookService tradeBookService;
    private final TradeRepository tradeRepository;
    private final UserService userService;

    public TradeController(TradeBookService tradeBookService, TradeRepository tradeRepository, UserService userService) {
        this.tradeBookService = tradeBookService;
        this.tradeRepository = tradeRepository;
        this.userService = userService;
    }

    @PostMapping("/sell")
    public ResponseEntity<?> placeSellOrder(@RequestBody Map<String, Object> payload) {
        try {
            Long sellerId = Long.valueOf(payload.get("sellerId").toString());
            String stockSymbol = payload.get("stockSymbol").toString();
            BigDecimal quantity = new BigDecimal(payload.get("quantity").toString());
            BigDecimal price = new BigDecimal(payload.get("price").toString());

            User seller = userService.getUserById(sellerId);

            Trade trade = new Trade();
            trade.setSeller(seller);
            trade.setStockSymbol(stockSymbol);
            trade.setQuantity(quantity);
            trade.setPrice(price);
            trade.setStatus("PENDING"); 
            
            Trade savedTrade = tradeRepository.save(trade);
            return ResponseEntity.ok(savedTrade);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<?> placeBuyOrder(@RequestBody Map<String, Object> payload) {
        try {
            Long buyerId = Long.valueOf(payload.get("buyerId").toString());
            Long tradeId = Long.valueOf(payload.get("tradeId").toString()); // Specifying which sell order to buy

            User buyer = userService.getUserById(buyerId);
            Trade trade = tradeRepository.findById(tradeId).orElseThrow(() -> new RuntimeException("Trade not found"));

            if (!"PENDING".equals(trade.getStatus())) {
                throw new RuntimeException("Trade is no longer pending");
            }

            trade.setBuyer(buyer);
            trade.setStatus("MATCHED");
            tradeRepository.save(trade);

            // Automatically trigger settlement as per the flow Requirements
            Settlement settlement = tradeBookService.executeSettlement(trade.getId());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "trade", trade,
                    "settlement", settlement
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/trades")
    public ResponseEntity<List<Trade>> getAllTrades() {
        return ResponseEntity.ok(tradeBookService.getAllTrades());
    }
}
