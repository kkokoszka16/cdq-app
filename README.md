# Transaction Aggregator

Banking transaction aggregation and statistics service for personal budget management.

## Overview

Transaction Aggregator is a REST API service that imports bank account transactions from CSV files and provides statistical aggregations by category, IBAN, and time period. The application enables tracking financial flows, analyzing spending patterns, and managing personal budgets through monthly transaction imports and comprehensive reporting capabilities.

### Core Functionality

The service accepts CSV files containing transaction records with IBAN, date, currency, category, and amount fields. Each import is tracked as a batch operation with status monitoring. Once imported, transactions can be queried with flexible filtering and retrieved as aggregated statistics by category, account, or time period.

### Key Features

- CSV transaction import with validation and duplicate detection via file checksums
- Batch import status tracking with progress monitoring
- Transaction queries with pagination and multi-criteria filtering
- Statistical aggregations by category, IBAN, and month
- Redis-based caching for statistics endpoints with 15-minute TTL
- Rate limiting at 100 requests per minute per client IP
- Production-ready observability with health checks, metrics, and Prometheus integration
- Graceful shutdown with in-flight request handling
- RFC 7807 Problem Details for standardized error responses

## Architecture

Transaction Aggregator implements hexagonal architecture with compile-time enforcement through a multi-module Maven structure. The architecture ensures strict separation between business logic and infrastructure concerns.

### Module Organization

The project consists of four Maven modules with enforced dependency direction from outer to inner layers:

**domain** module contains framework-free business entities and value objects. Key domain models include Transaction, ImportBatch, Money, Iban, and Category. The module defines domain exceptions such as InvalidIbanException, InvalidAmountException, and InvalidTransactionException. No Spring dependencies are permitted.

**application** module implements use cases and port interfaces. Services include TransactionImportService, TransactionQueryService, StatisticsService, and CsvParsingService. Port interfaces define contracts for persistence (TransactionRepository, ImportBatchRepository) and use case boundaries (ImportTransactionsUseCase, GetStatisticsUseCase, GetTransactionsUseCase). No Spring dependencies are permitted.

**infrastructure** module provides adapter implementations for ports. The MongoTransactionRepository and MongoImportBatchRepository adapters implement persistence using Spring Data MongoDB. REST controllers TransactionController and StatisticsController implement inbound adapters. Configuration classes include CacheConfig for Redis, SecurityConfig for CORS and headers, and RateLimitFilter for request throttling.

**bootstrap** module serves as the application entry point with TransactionAggregatorApplication class. It contains Spring Boot configuration and dependency injection wiring.

### Architecture Validation

The project includes ArchUnit tests in HexagonalArchitectureTest that verify module dependencies, ensure domain purity, validate port-adapter separation, and enforce naming conventions. The test suite runs automatically during the build phase.

### Technology Stack

- Java 21 with records, sealed interfaces, pattern matching, and text blocks
- Maven 3.8+ for build management
- Spring Boot 3.2.3 with Spring Web, Spring Data MongoDB, and Spring Cache
- MongoDB 7 for persistent storage
- Redis 7 for distributed caching
- MapStruct 1.5.5 for DTO mapping
- Lombok 1.18.30 for code generation
- SpringDoc OpenAPI 2.3.0 for API documentation
- Bucket4j 8.7.0 for rate limiting
- Zalando Problem Spring Web 0.29.1 for RFC 7807 error responses
- JUnit 5, AssertJ, and Mockito for testing
- Testcontainers 1.19.6 for integration tests

## Prerequisites

The following tools must be installed and available in your system PATH:

- Java Development Kit 21 or higher
- Maven 3.8 or higher
- Docker 24.0 or higher
- Docker Compose 2.20 or higher

Verify installations:

```bash
java -version
mvn -version
docker --version
docker compose version
```

## Installation

Clone the repository and navigate to the project directory:

```bash
git clone <repository-url>
cd cdq
```

The project includes sample CSV files in the `samples/` directory for testing purposes.

## Configuration

The application uses Spring Boot profiles for environment-specific configuration.

### Default Profile (Local Development)

Located in `bootstrap/src/main/resources/application.yml`:

- Server port: 8080
- MongoDB URI: mongodb://localhost:27017/bank_transactions
- Redis: localhost:6379
- Rate limit: 100 requests per minute
- Max file size: 10MB
- Max rows per import: 100,000
- Statistics cache TTL: 15 minutes

### Docker Profile

Located in `bootstrap/src/main/resources/application-docker.yml`:

- MongoDB URI: mongodb://mongodb:27017/bank_transactions
- Redis host: redis

### Custom Configuration

Override configuration properties using environment variables or command-line arguments:

```bash
java -jar bootstrap/target/bootstrap-1.0.0.jar \
  --spring.data.mongodb.uri=mongodb://custom-host:27017/db \
  --rate-limit.requests-per-minute=200
```

## Build

Build the entire multi-module project from the root directory:

```bash
mvn clean install
```

This command:

1. Compiles all four modules (domain, application, infrastructure, bootstrap)
2. Processes Lombok and MapStruct annotations
3. Executes unit tests in all modules
4. Executes integration tests with Testcontainers
5. Validates hexagonal architecture rules with ArchUnit
6. Packages the application as an executable JAR in `bootstrap/target/`

### Build Individual Modules

Compile a specific module:

```bash
mvn clean install -pl domain
mvn clean install -pl application -am
mvn clean install -pl infrastructure -am
```

The `-am` flag builds required dependencies automatically.

### Skip Tests

Build without running tests:

```bash
mvn clean install -DskipTests
```

### Build Artifacts

The build produces:

- `domain/target/domain-1.0.0.jar` - Domain model library
- `application/target/application-1.0.0.jar` - Application services library
- `infrastructure/target/infrastructure-1.0.0.jar` - Infrastructure adapters library
- `bootstrap/target/bootstrap-1.0.0.jar` - Executable Spring Boot application

### Docker Execution (Recommended)

Build and start the complete stack with one command:

```bash
docker compose up --build
```

This command:

1. Builds the multi-stage Docker image for the application
2. Starts MongoDB container with health checks
3. Starts Redis container with health checks
4. Waits for dependencies to become healthy
5. Starts the application container with health checks
6. Exposes port 8080 for API access

The application becomes available after the health check passes (approximately 60 seconds).

Monitor container logs:

```bash
docker compose logs -f app
docker compose logs -f mongodb
docker compose logs -f redis
```

Stop the stack:

```bash
docker compose down
```

Remove volumes and reset data:

```bash
docker compose down -v
```

### Health Check Verification

Check application health:

```bash
curl http://localhost:8080/actuator/health
```

Expected response for healthy state:

```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    },
    "livenessState": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    }
  }
}
```

## Test

The project includes comprehensive unit tests, integration tests, and architecture tests with over 60 test classes covering domain logic, application services, adapters, and API controllers.

### Run All Tests

Execute the complete test suite:

```bash
mvn test
```

### Run Integration Tests

Execute integration tests with Testcontainers:

```bash
mvn verify
```

### Run Tests for Specific Module

```bash
mvn test -pl domain
mvn test -pl application
mvn test -pl infrastructure
mvn test -pl bootstrap
```

### Swagger UI

Access the interactive API explorer at http://localhost:8080/swagger-ui.html

Swagger UI provides:

- Complete endpoint documentation with descriptions
- Request/response schema definitions
- Interactive request execution
- Example values for all parameters
- Authentication and authorization testing

### API Endpoints

The service exposes two controller groups.

**Transaction Operations** (`/api/v1/transactions`):

- `POST /api/v1/transactions/import` - Import CSV file with transactions (202 Accepted)
- `GET /api/v1/transactions/import/{importId}/status` - Get import batch status (200 OK)
- `GET /api/v1/transactions` - Query transactions with filters and pagination (200 OK)

**Statistics Operations** (`/api/v1/statistics`):

- `GET /api/v1/statistics/by-category?month=YYYY-MM` - Aggregate by category for month (200 OK)
- `GET /api/v1/statistics/by-iban?month=YYYY-MM` - Aggregate by IBAN for month (200 OK)
- `GET /api/v1/statistics/by-month?year=YYYY` - Aggregate by month for year (200 OK)

**Observability Endpoints** (`/actuator`):

- `GET /actuator/health` - Application health status
- `GET /actuator/health/liveness` - Kubernetes liveness probe
- `GET /actuator/health/readiness` - Kubernetes readiness probe
- `GET /actuator/metrics` - Available metrics list
- `GET /actuator/prometheus` - Prometheus-formatted metrics
- `GET /actuator/info` - Application information

All endpoints are protected by rate limiting at 100 requests per minute per client IP. Exceeding the limit returns HTTP 429 Too Many Requests.

### Import Transactions from CSV

First, create a test CSV file named `test_transactions.csv`:

```csv
iban,transactionDate,currency,category,amount
PL61109010140000071219812874,2024-01-15,PLN,FOOD,-125.50
PL61109010140000071219812874,2024-01-16,PLN,TRANSPORT,-45.00
PL27114020040000300201355387,2024-01-20,PLN,SALARY,5000.00
PL61109010140000071219812874,2024-01-25,PLN,ENTERTAINMENT,-80.00
PL27114020040000300201355387,2024-02-01,PLN,UTILITIES,-200.00
PL61109010140000071219812874,2024-02-05,PLN,FOOD,-95.00
```

Import the file:

```bash
curl -X POST http://localhost:8080/api/v1/transactions/import \
  -F "file=@test_transactions.csv" \
  -H "Content-Type: multipart/form-data"
```

Response:

```json
{
  "importId": "fb492c64-f021-47dc-9d8c-c8bb0c2c9928",
  "status": "PROCESSING",
  "message": "Import started"
}
```

### Check Import Status

Use the `importId` from the previous response:

```bash
curl -s http://localhost:8080/api/v1/transactions/import/fb492c64-f021-47dc-9d8c-c8bb0c2c9928/status | jq '.'
```

Response:

```json
{
  "importId": "fb492c64-f021-47dc-9d8c-c8bb0c2c9928",
  "status": "COMPLETED",
  "filename": "test_transactions.csv",
  "totalRows": 6,
  "successCount": 6,
  "errorCount": 0,
  "errors": [],
  "createdAt": "2026-01-25T20:57:35.954",
  "completedAt": "2026-01-25T20:57:36.037"
}
```

### List Transactions with Pagination

```bash
curl -s "http://localhost:8080/api/v1/transactions?page=0&size=10" | jq '.'
```

Response:

```json
{
  "content": [
    {
      "id": "ae70751d-7d82-4257-83ab-a80eac2ae3d1",
      "iban": "PL61109010140000071219812874",
      "transactionDate": "2024-02-05",
      "currency": "PLN",
      "category": "FOOD",
      "amount": -95.00,
      "importBatchId": "fb492c64-f021-47dc-9d8c-c8bb0c2c9928"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 6,
  "totalPages": 1
}
```

### Filter Transactions by IBAN and Category

```bash
curl -s "http://localhost:8080/api/v1/transactions?iban=PL61109010140000071219812874&category=FOOD" | jq '.'
```

Response:

```json
{
  "content": [
    {
      "id": "ae70751d-7d82-4257-83ab-a80eac2ae3d1",
      "iban": "PL61109010140000071219812874",
      "transactionDate": "2024-02-05",
      "currency": "PLN",
      "category": "FOOD",
      "amount": -95.00,
      "importBatchId": "fb492c64-f021-47dc-9d8c-c8bb0c2c9928"
    },
    {
      "id": "f2410d03-3730-49d8-ab52-2ad0452de557",
      "iban": "PL61109010140000071219812874",
      "transactionDate": "2024-01-15",
      "currency": "PLN",
      "category": "FOOD",
      "amount": -125.50,
      "importBatchId": "fb492c64-f021-47dc-9d8c-c8bb0c2c9928"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1
}
```

### Statistics by Category

```bash
curl -s "http://localhost:8080/api/v1/statistics/by-category?month=2024-01" | jq '.'
```

Response:

```json
{
  "month": "2024-01",
  "categories": [
    {
      "category": "ENTERTAINMENT",
      "totalAmount": -80.00,
      "transactionCount": 1
    },
    {
      "category": "FOOD",
      "totalAmount": -125.50,
      "transactionCount": 1
    },
    {
      "category": "SALARY",
      "totalAmount": 5000.00,
      "transactionCount": 1
    },
    {
      "category": "TRANSPORT",
      "totalAmount": -45.00,
      "transactionCount": 1
    }
  ]
}
```

### Statistics by IBAN

```bash
curl -s "http://localhost:8080/api/v1/statistics/by-iban?month=2024-01" | jq '.'
```

Response:

```json
{
  "month": "2024-01",
  "ibans": [
    {
      "iban": "PL27114020040000300201355387",
      "totalIncome": 5000.00,
      "totalExpense": 0,
      "balance": 5000.00
    },
    {
      "iban": "PL61109010140000071219812874",
      "totalIncome": 0,
      "totalExpense": -250.50,
      "balance": -250.50
    }
  ]
}
```

### Statistics by Month

```bash
curl -s "http://localhost:8080/api/v1/statistics/by-month?year=2024" | jq '.'
```

Response:

```json
{
  "year": 2024,
  "months": [
    {
      "month": "2024-01",
      "totalIncome": 5000.00,
      "totalExpense": -250.50,
      "balance": 4749.50
    },
    {
      "month": "2024-02",
      "totalIncome": 0,
      "totalExpense": -295.00,
      "balance": -295.00
    }
  ]
}
```

### Error Response Format

All errors follow RFC 7807 Problem Details format:

```bash
curl -X POST http://localhost:8080/api/v1/transactions/import \
  -F "file=@invalid.txt" \
  -H "Content-Type: multipart/form-data"
```

Response (400 Bad Request):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "File must be CSV format",
  "instance": "/api/v1/transactions/import"
}
```

Rate limit exceeded response (429 Too Many Requests):

```json
{
  "type": "about:blank",
  "title": "Too Many Requests",
  "status": 429,
  "detail": "Rate limit exceeded: 100 requests per minute",
  "instance": "/api/v1/transactions/import"
}
```