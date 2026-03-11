# InstaSettle - Real-Time Blockchain Settlement Layer

InstaSettle is a working prototype demonstrating how blockchain can reduce stock settlement time from T+1 to T+0 (near instant, <5 minutes) using Atomic Delivery vs Payment (DvP).

## Architecture
- **Smart Contracts (Solidity/Hardhat):** `INRToken.sol`, `SecurityToken.sol`, `SettlementContract.sol` handling the atomic swaps.
- **Backend (Spring Boot 21):** Exposes REST APIs, manages order book, and triggers blockchain settlements via Web3j.
- **Frontend (React + Vite):** A responsive Dashboard to place orders and monitor the settlement process in real-time.
- **Database (MySQL):** Stores traditional off-chain data (User details, trade intents) while the blockchain handles the actual asset transfer.

## Prerequisites
- **Docker** and **Docker Compose**

## How to Run
Everything is containerized and orchestrated via Docker Compose. You **do not** need to install Node, Java, or MySQL manually.

1. Open a terminal in the root `instasettle` directory.
2. Run the following command:
   ```bash
   docker-compose up --build
   ```
3. Wait for all containers to start. The startup sequence is:
   - MySQL initializes the schema.
   - Hardhat boots a local Ethereum node and deploys the contracts (creating sample users and balances).
   - Spring Boot connects to MySQL and Hardhat.
   - React Frontend boots up.

4. Once running, access the dashboard: **http://localhost:3000** 

## Demo Scenario
By default, 3 accounts (Alice, Bob, Charlie) are automatically seeded with `REL_TOKEN`, `TCS_TOKEN`, and `INR` balances upon startup.

1. Select **Alice** from the active users in the top right.
2. **Alice** places a **SELL** order for `REL` (e.g., 10 shares @ ₹2500).
3. Switch user to **Bob**.
4. **Bob** clicks "Deposit ₹10,000" to simulate a UPI fiat deposit (which mints INR_TOKEN on-chain).
5. **Bob** places a **BUY** order for `REL` (10 shares @ ₹2500).
6. **Watch the magic:** The Backend order matching engine pairs the orders and triggers the `SettlementContract`. Both the `REL_TOKEN` and `INR_TOKEN` are swapped atomically.
7. The trade instantly shows as **SETTLED** (T+0).
