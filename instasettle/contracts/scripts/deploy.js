const hre = require("hardhat");
const fs = require("fs");

async function main() {
  const [deployer, user1, user2, user3] = await hre.ethers.getSigners();
  console.log("Deploying contracts with the account:", deployer.address);

  const INRToken = await hre.ethers.deployContract("INRToken");
  await INRToken.waitForDeployment();
  console.log("INRToken deployed to:", INRToken.target);

  const RELToken = await hre.ethers.deployContract("SecurityToken", ["Reliance Industries", "REL"]);
  await RELToken.waitForDeployment();
  console.log("RELToken deployed to:", RELToken.target);

  const TCSToken = await hre.ethers.deployContract("SecurityToken", ["Tata Consultancy Services", "TCS"]);
  await TCSToken.waitForDeployment();
  console.log("TCSToken deployed to:", TCSToken.target);

  const Settlement = await hre.ethers.deployContract("SettlementContract", [INRToken.target]);
  await Settlement.waitForDeployment();
  console.log("SettlementContract deployed to:", Settlement.target);

  // Seed Data: Give users some predefined tokens
  const users = [user1, user2, user3];
  
  for (let i = 0; i < users.length; i++) {
    // Mint 100,000 INR (2 decimals = 10,000,000 units)
    await INRToken.mint(users[i].address, 10000000);
    // Mint 50 REL and 50 TCS
    await RELToken.mint(users[i].address, 50);
    await TCSToken.mint(users[i].address, 50);
    
    // Approve the Settlement contract to spend their tokens
    await INRToken.connect(users[i]).approve(Settlement.target, hre.ethers.MaxUint256);
    await RELToken.connect(users[i]).approve(Settlement.target, hre.ethers.MaxUint256);
    await TCSToken.connect(users[i]).approve(Settlement.target, hre.ethers.MaxUint256);
  }

  // Write addresses and mock user keys/addresses for backend to use
  const config = {
    INRToken: INRToken.target,
    RELToken: RELToken.target,
    TCSToken: TCSToken.target,
    SettlementContract: Settlement.target,
    Deployer: deployer.address,
    Users: [
      { address: user1.address },
      { address: user2.address },
      { address: user3.address }
    ]
  };

  fs.writeFileSync('/app/shared/contract-addresses.json', JSON.stringify(config, null, 2));
  console.log("Seed data applied and shared/contract-addresses.json created.");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
