package com.instasettle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

@Service
public class BlockchainService {

    private final Web3j web3j;
    
    @Value("${contracts.path}")
    private String contractsPath;

    private String inrTokenAddress;
    private String relTokenAddress;
    private String tcsTokenAddress;
    private String settlementContractAddress;
    private String deployerPrivateKey = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80"; // Default Hardhat Account #0

    public BlockchainService(Web3j web3j) {
        this.web3j = web3j;
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(contractsPath);
            if (file.exists()) {
                JsonNode rootNode = mapper.readTree(file);
                this.inrTokenAddress = rootNode.get("INRToken").asText();
                this.relTokenAddress = rootNode.get("RELToken").asText();
                this.tcsTokenAddress = rootNode.get("TCSToken").asText();
                this.settlementContractAddress = rootNode.get("SettlementContract").asText();
                System.out.println("Loaded contract addresses from " + contractsPath);
            } else {
                System.err.println("Warning: Contract addresses file not found at " + contractsPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to parse contract addresses: " + e.getMessage());
        }
    }

    public String settleTradeOnChain(Long tradeId, String buyerAddress, String sellerAddress, String stockSymbol, BigInteger shareAmount, BigInteger price) throws Exception {
        String shareTokenAddress = stockSymbol.equalsIgnoreCase("REL") ? relTokenAddress : tcsTokenAddress;

        Function settleFunction = new Function(
                "settleTrade",
                Arrays.asList(
                        new Uint256(BigInteger.valueOf(tradeId)),
                        new Address(buyerAddress),
                        new Address(sellerAddress),
                        new Address(shareTokenAddress),
                        new Uint256(shareAmount),
                        new Uint256(price)
                ),
                Collections.emptyList()
        );

        String encodedFunction = FunctionEncoder.encode(settleFunction);
        
        Credentials credentials = Credentials.create(deployerPrivateKey);
        TransactionManager txManager = new RawTransactionManager(web3j, credentials, 31337);

        EthSendTransaction ethSendTransaction = txManager.sendTransaction(
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT,
                settlementContractAddress,
                encodedFunction,
                BigInteger.ZERO
        );

        if (ethSendTransaction.hasError()) {
            throw new RuntimeException("Error settling trade on blockchain: " + ethSendTransaction.getError().getMessage());
        }

        return ethSendTransaction.getTransactionHash();
    }
    
    public String getInrTokenAddress() {
        return inrTokenAddress;
    }
}
