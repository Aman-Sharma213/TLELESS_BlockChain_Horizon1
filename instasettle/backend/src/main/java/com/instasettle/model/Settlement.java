package com.instasettle.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Data
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "trade_id", unique = true)
    private Trade trade;

    @Column(name = "block_hash")
    private String blockHash;
    
    @Column(name = "transaction_hash")
    private String transactionHash;
    
    private LocalDateTime timestamp;
    private String status;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
