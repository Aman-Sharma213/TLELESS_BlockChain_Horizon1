// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract SecurityToken is ERC20, Ownable {
    constructor(string memory name, string memory symbol) ERC20(name, symbol) Ownable(msg.sender) {}

    function mint(address to, uint256 amount) public onlyOwner {
        _mint(to, amount);
    }
    
    function decimals() public view virtual override returns (uint8) {
        // Stocks usually have no decimal places, but we can set it to 0 or 2.
        // Let's use 0 for whole shares.
        return 0;
    }
}
