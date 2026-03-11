FROM node:20-alpine AS builder

WORKDIR /app
COPY package.json ./
RUN npm install

COPY . .
RUN npm run build

# Use a lightweight node server to host Vite build (or use NGINX)
# In dev setup, we can also use 'npm run dev' but building is cleaner
# We will just run standard `npx serve`
RUN npm install -g serve
EXPOSE 3000

CMD ["serve", "-s", "dist", "-l", "3000"]
