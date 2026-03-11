// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract SettlementContract is Ownable {
    IERC20 public inrToken;

    event TradeSettled(
        uint256 indexed tradeId,
        address indexed buyer,
        address indexed seller,
        address shareToken,
        uint256 shareAmount,
        uint256 price
    );

    constructor(address _inrToken) Ownable(msg.sender) {
        inrToken = IERC20(_inrToken);
    }

    function settleTrade(
        uint256 tradeId,
        address buyer,
        address seller,
        address shareToken,
        uint256 shareAmount,
        uint256 price
    ) external {
        require(inrToken.transferFrom(buyer, seller, price), "INR payment failed");
        
        IERC20 security = IERC20(shareToken);
        require(security.transferFrom(seller, buyer, shareAmount), "Security delivery failed");

        emit TradeSettled(tradeId, buyer, seller, shareToken, shareAmount, price);
    }
}
