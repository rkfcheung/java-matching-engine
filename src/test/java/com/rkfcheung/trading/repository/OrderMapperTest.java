package com.rkfcheung.trading.repository;

import com.rkfcheung.trading.api.OrderStatus;
import com.rkfcheung.trading.model.BidPrice;
import com.rkfcheung.trading.model.Order;
import com.rkfcheung.trading.model.Price;
import com.rkfcheung.trading.model.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private OrderMapper orderMapper;
    private UUID id;
    private UUID clientId;
    private UUID instrumentId;
    private Instant createdAt;

    @BeforeEach
    void setup() {
        orderMapper = new OrderMapper();
        id = UUID.randomUUID();
        clientId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
        createdAt = Instant.now();
    }

    @Test
    void testToEntityWithLimitOrder() {
        var price = new BidPrice(BigDecimal.valueOf(100.5));
        var order = new Order(
                id,
                Side.BID,
                instrumentId,
                price,
                10,
                false,
                clientId,
                createdAt
        );

        var entity = orderMapper.toEntity(order);

        assertEquals(id, entity.getId());
        assertEquals(clientId, entity.getClientId());
        assertEquals(instrumentId, entity.getInstrumentId());
        assertEquals(100.5, entity.getPrice());
        assertEquals(10, entity.getQuantity());
        assertEquals(Side.BID, entity.getSide());
        assertFalse(entity.isMarketOrder());
        assertEquals(OrderStatus.PENDING, entity.getOrderStatus());
        assertEquals(createdAt, entity.getCreatedAt());
        assertNull(entity.getExecutionPrice());
        assertNull(entity.getExecutedAt());
        assertNull(entity.getCancelledAt());
    }

    @Test
    void testToEntityWithMarketOrder() {
        var price = Price.of(Side.ASK, null);
        var order = new Order(
                id,
                Side.ASK,
                instrumentId,
                price,
                5,
                true,
                clientId,
                createdAt
        );

        var entity = orderMapper.toEntity(order);

        assertNull(entity.getPrice());
        assertTrue(entity.isMarketOrder());
    }

    @Test
    void testToDomainWithLimitOrder() {
        var entity = new OrderEntity();
        entity.setId(id);
        entity.setClientId(clientId);
        entity.setInstrumentId(instrumentId);
        entity.setPrice(55.5);
        entity.setQuantity(7);
        entity.setSide(Side.BID);
        entity.setMarketOrder(false);
        entity.setCreatedAt(createdAt);

        var order = orderMapper.toDomain(entity);

        assertEquals(id, order.id());
        assertEquals(clientId, order.clientId());
        assertEquals(instrumentId, order.instrumentId());
        assertEquals(7, order.quantity());
        assertEquals(Side.BID, order.side());
        assertFalse(order.isMarketOrder());
        assertNotNull(order.price());
        assertEquals(BigDecimal.valueOf(55.5), order.price().value());
        assertEquals(createdAt, order.createdAt());
    }

    @Test
    void testToDomainWithMarketOrder() {
        var entity = new OrderEntity();
        entity.setId(id);
        entity.setClientId(clientId);
        entity.setInstrumentId(instrumentId);
        entity.setPrice(null);
        entity.setQuantity(3);
        entity.setSide(Side.ASK);
        entity.setMarketOrder(true);
        entity.setCreatedAt(createdAt);

        var order = orderMapper.toDomain(entity);

        assertTrue(order.isMarketOrder());
        assertNull(order.price().value());
        assertEquals(Side.ASK, order.side());
    }
}
