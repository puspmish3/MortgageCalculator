# Multi-stage build for Vue.js application
FROM node:20-alpine AS build

# Set the working directory
WORKDIR /app

# Copy package files for dependency caching
COPY package*.json ./

# Install dependencies (including dev dependencies for build)
RUN npm ci

# Copy source code
COPY . .

# Build the application (skip type checking for now)
RUN npm run build-only

# Production stage with nginx
FROM nginx:alpine

# Copy built application from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]