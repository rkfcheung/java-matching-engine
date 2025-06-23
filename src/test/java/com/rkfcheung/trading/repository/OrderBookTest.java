package com.rkfcheung.trading.repository;

import com.rkfcheung.trading.model.BidPrice;
import com.rkfcheung.trading.model.Order;
import com.rkfcheung.trading.model.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class OrderBookTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderBook orderBook;

    private UUID clientId;
    private Order order;
    private OrderEntity entity;

    @BeforeEach
    void setUp() {
        openMocks(this);

        clientId = UUID.randomUUID();
        order = new Order(UUID.randomUUID(), Side.BID, UUID.randomUUID(), new BidPrice(null), 10L, true, clientId, Instant.now());

        entity = new OrderEntity();
        entity.setId(order.id());
        entity.setClientId(order.clientId());
        entity.setInstrumentId(order.instrumentId());
    }

    @Test
    void testAddOrderSuccess() {
        // Given
        when(orderMapper.toEntity(order)).thenReturn(entity);
        when(orderRepository.insert(entity)).thenReturn(Mono.just(entity));
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // When
        var result = orderBook.add(order);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(o -> o.id().equals(order.id()))
                .verifyComplete();

        verify(orderMapper).toEntity(order);
        verify(orderRepository).insert(entity);
        verify(orderMapper).toDomain(entity);
    }

    @Test
    void testCancelOrderSuccess() {
        // Given
        testAddOrderSuccess();
        when(orderRepository.findPending(order.id(), clientId)).thenReturn(Mono.just(entity));
        when(orderMapper.toDomain(entity)).thenReturn(order);
        when(orderRepository.cancel(order.id())).thenReturn(Mono.just(Instant.now()));

        // When
        var result = orderBook.cancel(clientId, order.id());

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(orderRepository).findPending(order.id(), clientId);
        verify(orderRepository).cancel(order.id());
    }

    @Test
    void testCancelOrderNotFound() {
        // Given
        when(orderRepository.findPending(order.id(), clientId)).thenReturn(Mono.just(entity));
        when(orderMapper.toDomain(entity)).thenReturn(order);

        // When
        var result = orderBook.cancel(clientId, order.id());

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(orderRepository).findPending(order.id(), clientId);
        verify(orderRepository, never()).cancel(any());
    }
}
