# ---- Build Stage ----
FROM node:21-alpine AS build
WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm ci --frozen-lockfile

# Build application
COPY . .
RUN npm run build

# ---- Runtime Stage ----
FROM node:21-alpine
WORKDIR /app

RUN npm install -g serve && \
    addgroup -S app && \
    adduser -S app -G app

COPY --from=build /app/dist ./dist
USER app

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:3000 || exit 1

CMD ["serve", "-s", "dist", "-l", "3000"]
