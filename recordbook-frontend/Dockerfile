# Use the official Node.js 14 image.
FROM node:14

# Set the working directory in the container.
WORKDIR /app

# Copy package.json and package-lock.json for dependency installation.
COPY package*.json ./

# Install the application dependencies.
RUN npm ci

# Copy the rest of the application files.
COPY . .

# Expose the API port.
EXPOSE 3000

# Start the application.
CMD [ "npm", "start" ]
