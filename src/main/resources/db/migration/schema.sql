CREATE TABLE orders (
    id UUID PRIMARY KEY,
    client_id UUID,
    instrument_id UUID,
    price DOUBLE,
    quantity BIGINT,
    side VARCHAR(3),
    is_market_order BOOLEAN,
    order_status VARCHAR(16),
    execution_price DOUBLE,
    created_at TIMESTAMP,
    executed_at TIMESTAMP,
    cancelled_at TIMESTAMP
);
