package com.instasettle.controller;

import com.instasettle.model.User;
import com.instasettle.service.BlockchainService;
import com.instasettle.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final BlockchainService blockchainService;
    private final Web3j web3j;
    private final String deployerPrivateKey = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";

    public UserController(UserService userService, BlockchainService blockchainService, Web3j web3j) {
        this.userService = userService;
        this.blockchainService = blockchainService;
        this.web3j = web3j;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> payload) {
        try {
            User user = userService.registerUser(payload.get("name"), payload.get("email"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            
            User user = userService.getUserById(userId);
            
            // Amount in smallest unit (paise = amount * 100)
            BigInteger amountInPaise = amount.multiply(new BigDecimal("100")).toBigInteger();

            // Setup transaction to call mint on INRToken
            Function mintFunction = new Function(
                    "mint",
                    Arrays.asList(new Address(user.getWalletAddress()), new Uint256(amountInPaise)),
                    Collections.emptyList()
            );

            String encodedFunction = FunctionEncoder.encode(mintFunction);
            Credentials credentials = Credentials.create(deployerPrivateKey);
            TransactionManager txManager = new RawTransactionManager(web3j, credentials, 31337);

            EthSendTransaction ethSendTransaction = txManager.sendTransaction(
                    DefaultGasProvider.GAS_PRICE,
                    DefaultGasProvider.GAS_LIMIT,
                    blockchainService.getInrTokenAddress(),
                    encodedFunction,
                    BigInteger.ZERO
            );

            if (ethSendTransaction.hasError()) {
                throw new RuntimeException("Mint failed: " + ethSendTransaction.getError().getMessage());
            }

            return ResponseEntity.ok(Map.of("status", "success", "txHash", ethSendTransaction.getTransactionHash()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
