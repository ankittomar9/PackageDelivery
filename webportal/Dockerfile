FROM node:20-alpine

WORKDIR /app

# Copy package locks and install dependencies
COPY package*.json ./
RUN npm install

# Copy source code
COPY . .

# Expose Vite dev server port
EXPOSE 5173

# Launch Vite server exposing host interface for Docker bridge network
CMD ["npm", "run", "dev", "--", "--host"]
