# Simple Java Matching Engine

Simple Matching Engine for demo purpose:

* Support Market and Limit orders
* Only support fully matching orders
* No authentication or authorisation logic
* Use Spring WebFlux which will be extended to event-driven easier

## Getting Started

1. Install JDK 21
2. Clone the repo and execute

```bash
git clone git@github.com:rkfcheung/java-matching-engine.git
cd java-matching-engine
./gradlew bootRun
```

## Sample Requests

### Submit a New Limit Buy Order

```bash
http POST localhost:8080/orders \
  client-id:123e4567-e89b-12d3-a456-426614174000 \
  orderType=BUY \
  instrumentId=123e4567-e89b-12d3-a456-426614174001 \
  price:=100.0 \
  quantity:=10
```

```json
{
  "executionPrice": null,
  "orderId": "3c972b5b-45a6-408c-a97b-b03939f22d85",
  "orderStatus": "PENDING",
  "timestamp": "2025-06-23T21:41:16.818514548Z",
  "validationError": null
}
```

### Submit a New Market Sell Order and Execute if Matched

```bash
http POST localhost:8080/orders \
  client-id:123e4567-e89b-12d3-a456-426614174000 \
  orderType=SELL \
  instrumentId=123e4567-e89b-12d3-a456-426614174001 \
  quantity:=10
```

```json
{
  "executionPrice": 100.0,
  "orderId": "d961a9ee-1292-4475-8301-e22ee9acecec",
  "orderStatus": "EXECUTED",
  "timestamp": "2025-06-23T22:01:45.569216891Z",
  "validationError": null
}
```

### Cancel an Existing Order

```bash
http PUT localhost:8080/orders/3c972b5b-45a6-408c-a97b-b03939f22d85 \
  client-id:123e4567-e89b-12d3-a456-426614174000
```

```json
{
  "reason": null,
  "success": true
}
```

More tests can be found
at [OrderControllerE2ETest](src/test/java/com/rkfcheung/trading/integration/OrderControllerE2ETest.java).

## Development

System Information

* Java: OpenJDK 21.0.7
* Spring Boot: 3.5.3
* OS: Linux (Debian 6.1.0-37-amd64)
