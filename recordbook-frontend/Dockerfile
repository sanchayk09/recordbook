# Use Node 20 (LTS) - much more stable for modern React/Frontend apps
FROM node:20-slim

WORKDIR /app

# Copy package files
COPY package*.json ./

# npm install is safer here if your lockfile was made on a different OS/Node version
RUN npm install

COPY . .

# Expose port 3000 (React default)
EXPOSE 3000

CMD [ "npm", "start" ]