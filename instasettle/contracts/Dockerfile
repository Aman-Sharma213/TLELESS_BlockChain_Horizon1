FROM node:20-alpine

WORKDIR /app

COPY package.json ./
RUN npm install

COPY . .

# Run hardhat node on port 8545
EXPOSE 8545

# Create shared volume mount point
RUN mkdir -p /app/shared

# Command to startup hardhat and execute deployment script in background
# (Deploy script writes contract-address.json to /app/shared for backend)
CMD ["sh", "-c", "npx hardhat node > hardhat.log 2>&1 & sleep 5 && npx hardhat run scripts/deploy.js --network localhost && tail -f hardhat.log"]
