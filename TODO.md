# InstaSettle Error Correction Plan

## Issues to Fix:
- [ ] 1. CORS Mismatch: Backend allows port 5173 but frontend runs on 3000
- [ ] 2. Smart Contract Access Control: Only owner can call settleTrade 
- [ ] 3. No User Registration Flow: Frontend never registers users
- [ ] 4. BlockchainService null addresses if config file missing

## Implementation Steps:
1. Update CorsConfig.java - ensure port 3000 is allowed (verify)
2. Update SettlementContract.sol - remove onlyOwner restriction  
3. Update App.jsx - add user registration on startup
4.
