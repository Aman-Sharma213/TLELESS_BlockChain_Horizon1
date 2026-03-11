package com.instasettle.service;

import com.instasettle.model.Settlement;
import com.instasettle.model.Trade;
import com.instasettle.model.User;
import com.instasettle.repository.SettlementRepository;
import com.instasettle.repository.TradeRepository;
import com.instasettle.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Service
public class TradeBookService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final SettlementRepository settlementRepository;
    private final BlockchainService blockchainService;

    public TradeBookService(TradeRepository tradeRepository, UserRepository userRepository, 
                            SettlementRepository settlementRepository, BlockchainService blockchainService) {
        this.tradeRepository = tradeRepository;
        this.userRepository = userRepository;
        this.settlementRepository = settlementRepository;
        this.blockchainService = blockchainService;
    }

    @Transactional
    public Trade placeTrade(Long buyerId, Long sellerId, String stockSymbol, BigDecimal quantity, BigDecimal price) {
        User buyer = userRepository.findById(buyerId).orElseThrow(() -> new RuntimeException("Buyer not found"));
        User seller = userRepository.findById(sellerId).orElseThrow(() -> new RuntimeException("Seller not found"));

        Trade trade = new Trade();
        trade.setBuyer(buyer);
        trade.setSeller(seller);
        trade.setStockSymbol(stockSymbol);
        trade.setQuantity(quantity);
        trade.setPrice(price);
        trade.setStatus("MATCHED"); 
        
        return tradeRepository.save(trade);
    }

    @Transactional
    public Settlement executeSettlement(Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId).orElseThrow(() -> new RuntimeException("Trade not found"));
        
        if (!"MATCHED".equals(trade.getStatus())) {
            throw new RuntimeException("Trade is not in MATCHED state");
        }

        trade.setStatus("SETTLING");
        tradeRepository.save(trade);

        try {
            // Convert to smallest units (wei/paise respectively)
            // Assuming 0 decimals for Stock Token and 2 decimals for INR Token (defined in contracts)
            BigInteger shareAmount = trade.getQuantity().toBigInteger();
            BigInteger inrAmount = trade.getPrice().multiply(trade.getQuantity()).multiply(new BigDecimal("100")).toBigInteger();

            // Settle on blockchain
            long startTime = System.currentTimeMillis();
            String txHash = blockchainService.settleTradeOnChain(
                    trade.getId(),
                    trade.getBuyer().getWalletAddress(),
                    trade.getSeller().getWalletAddress(),
                    trade.getStockSymbol(),
                    shareAmount,
                    inrAmount
            );
            long timeTaken = System.currentTimeMillis() - startTime;
            
            System.out.println("Settlement successful. TxHash: " + txHash + " Time: " + timeTaken + "ms");

            // Record settlement
            Settlement settlement = new Settlement();
            settlement.setTrade(trade);
            settlement.setTransactionHash(txHash);
            settlement.setBlockHash("PENDING"); // You could use web3j to wait for receipt to get real block hash
            settlement.setStatus("SUCCESS");
            
            settlement = settlementRepository.save(settlement);

            trade.setStatus("SETTLED");
            tradeRepository.save(trade);

            return settlement;

        } catch (Exception e) {
            trade.setStatus("FAILED");
            tradeRepository.save(trade);
            throw new RuntimeException("Settlement failed: " + e.getMessage(), e);
        }
    }
    
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }
}
