# TradeXchange - Spring Boot Trading Platform Backend

TradeXchange is an event-driven, high-performance financial exchange matching engine and real-time trading backend converted from Node.js/Express to **Java / Spring Boot 3**.

---

## 🌟 Key Architecture & Capabilities

1. **High-Performance In-Memory Matching Engine**:
   - Double-sided order book with bid max-heap and ask min-heap (`PriorityQueue`).
   - Price-time priority with FIFO sequence ordering.
   - Per-symbol granular concurrency locks (`ReentrantLock`).
   - Full support for `LIMIT` and `MARKET` orders.
   - Partial fills, multi-order execution, and VWAP average filled price calculation.

2. **Real-time Event Streaming (Socket.IO v4 & WebSocket)**:
   - Built-in Engine.IO / Socket.IO v4 protocol bridge over WebSocket.
   - Seamless plug-and-play compatibility with `socket.io-client` in the React frontend.
   - Real-time rooms for symbol subscriptions:
     - `trade`: Instant trade execution stream.
     - `depth`: Throttled (300ms) aggregated order book depth.
     - `candle`: 1-minute live candlestick updates.
     - `ticker`: Real-time symbol price ticks.

3. **Data Persistence (Zero-Setup H2 & Production MySQL)**:
   - **Default profile**: In-memory H2 database with automatic schema creation and web console (`http://localhost:5000/h2-console`).
   - **MySQL profile**: Compatible with MySQL 8.x via `-Dspring.profiles.active=mysql`.
   - **Automated Seeder**: Pre-seeds 68+ crypto pairs (BTC-INR, ETH-INR, SOL-INR), US stocks (AAPL, TSLA, AMZN), Indian stocks (RELIANCE, TCS, INFY), and commodities (GOLD, SILVER, CRUDEOIL).

4. **API & Developer Tooling**:
   - Interactive Swagger / OpenAPI 3.0 documentation at `http://localhost:5000/swagger-ui.html`.
   - Comprehensive JUnit 5 unit and concurrency test suite.

---

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher (JDK 21, 23 supported)
- Maven 3.8+ (or use the included `./mvnw` / `mvnw.cmd`)

### 1. Run with In-Memory H2 (Default, Zero-Setup)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
The server will start on port `5000` (matching the default React frontend expectations).

### 2. Run with MySQL
Ensure MySQL is running on port 3306 with database `tradexchange`:
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```
Or set environment variables:
```bash
set MYSQL_HOST=127.0.0.1
set MYSQL_PORT=3306
set MYSQL_DB=tradexchange
set MYSQL_USER=root
set MYSQL_PASSWORD=your_password
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 3. Run Automated Tests
```bash
.\mvnw.cmd test
```
All 12 matching engine tests and application context tests will execute.

### 4. Build Production JAR
```bash
.\mvnw.cmd clean package -DskipTests
java -jar target/tradexchange-backend-1.0.0.jar
```

---

## 🌐 Endpoints & API Reference

Base URL: `http://localhost:5000/api/v1`

### 1. Health
- `GET /health` -> `{"success": true, "status": "OK"}`

### 2. Orders
- `POST /api/v1/orders` -> Place LIMIT or MARKET order
  ```json
  {
    "userId": "user_1",
    "symbol": "BTC-INR",
    "side": "BUY",
    "type": "LIMIT",
    "price": 6500000,
    "quantity": 0.5
  }
  ```
  Response:
  ```json
  {
    "success": true,
    "orderId": "3b2e5...-..."
  }
  ```
- `GET /api/v1/orders?userId=...&symbol=...&status=...&limit=100` -> Get orders list
- `GET /api/v1/orders/{id}` -> Get single order details
- `DELETE /api/v1/orders/{id}` -> Cancel an open order
- `GET /api/v1/depth/{symbol}` -> Get order book depth (bids, asks, lastTradedPrice)

### 3. Trades
- `GET /api/v1/trades?symbol=BTC-INR&limit=50` -> Get recent trades
- `GET /api/v1/trades/{id}` -> Get single trade
- `GET /api/v1/trades/order/{orderId}` -> Get trades executed for an order
- `GET /api/v1/trades/symbol/{symbol}` -> Get trades by symbol

### 4. Markets & Charts
- `GET /api/v1/markets?type=crypto&search=BTC` -> List markets
- `GET /api/v1/markets/{symbol}` -> Single market specifications & trading rules
- `GET /api/v1/markets/type/{type}` -> Filter by `crypto`, `stock`, or `commodity`
- `GET /api/v1/markets/chart?symbol=BTC-INR` -> Historical OHLC chart candles
- `GET /api/v1/candles?symbol=BTC-INR&interval=1m&limit=200` -> Real OHLC candlestick bucket data

### 5. User & Watchlist
- `GET /api/v1/user/profile?userId=...` -> User profile and account status
- `GET /api/v1/user/watchlist?userId=...` -> User saved market watchlist
- `POST /api/v1/user/watchlist` -> Add/remove symbol in watchlist
  ```json
  {
    "userId": "user_1",
    "symbol": "ETH-INR"
  }
  ```

---

## ⚡ WebSocket / Socket.IO Integration

- **WebSocket URL**: `ws://localhost:5000/socket.io/?EIO=4&transport=websocket`
- **Standard WebSocket URL**: `ws://localhost:5000/ws`
- **Subscription Action**: emit `"subscribe", "BTC-INR"`
- **Unsubscription Action**: emit `"unsubscribe", "BTC-INR"`
- **Streams Received**:
  - `trade`: `{ id, symbol, price, quantity, buyOrderId, sellOrderId, side, timestamp }`
  - `ticker`: `{ symbol, price }`
  - `depth`: `{ symbol, bids: [{price, quantity}], asks: [{price, quantity}] }`
  - `candle`: `{ symbol, interval: "1m", candle: { time, open, high, low, close, volume } }`
