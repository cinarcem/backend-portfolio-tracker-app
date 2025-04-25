# Portfolio Tracker App Backend API - ReadMe

## Overview
The Portfolio Tracker App Backend API is a RESTful service designed to help users monitor their stock portfolios and track market indices.

## Key Features

### 📈 Market Data Access
- Retrieve prices for stocks and indices
- Access current market information

### 💼 Portfolio Management
- Save and track user-specific stock transactions
- Calculate current portfolio value
- Review historical portfolio transactions

### 👀 Watchlist Functionality
- Create personalized watchlists
- Track stocks and indices of interest

### 🔒 Security
- Keycloak integration for secure authentication

### ⚙️ Architecture
- **Microservices-based design**
    - **Config Server**: Centralized configuration management for all services
    - **API Gateway** (Spring Cloud Gateway): Request routing and load balancing
    - **Service Discovery** (Eureka): Dynamic registration and discovery of microservices
- Environment-specific configuration support (dev, prod, etc.)

## Important Notice
⚠️ **This API is for demonstration purposes only**  
It is not intended for commercial or personal use. The project was developed to showcase coding abilities and architectural understanding.
