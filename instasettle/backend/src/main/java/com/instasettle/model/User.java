package com.instasettle.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(unique = true)
    private String email;
    
    @Column(name = "wallet_address")
    private String walletAddress;
    
    @Column(name = "private_key")
    private String privateKey;
}
