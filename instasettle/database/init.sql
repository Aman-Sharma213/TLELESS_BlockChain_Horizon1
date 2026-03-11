CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    wallet_address VARCHAR(255) NOT NULL,
    private_key VARCHAR(255) NOT NULL
);

CREATE TABLE trades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT,
    seller_id BIGINT,
    stock_symbol VARCHAR(50) NOT NULL,
    quantity DECIMAL(19, 4) NOT NULL,
    price DECIMAL(19, 4) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_id BIGINT UNIQUE NOT NULL,
    block_hash VARCHAR(255) NOT NULL,
    transaction_hash VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (trade_id) REFERENCES trades(id)
);

-- Seed Data using Hardhat's default test accounts 1, 2, and 3
INSERT INTO users (name, email, wallet_address, private_key) VALUES
('Alice', 'alice@test.com', '0x70997970C51812dc3A010C7d01b50e0d17dc79C8', '0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d'),
('Bob', 'bob@test.com', '0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC', '0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a'),
('Charlie', 'charlie@test.com', '0x90F79bf6EB2c4f870365E785982E1f101E93b906', '0x7c852118294e51e653712a81e05800f419141751be58f605c371e15141b007a6');
