package com.rkfcheung.trading.service;

import com.rkfcheung.trading.model.AskPrice;
import com.rkfcheung.trading.model.BidPrice;
import com.rkfcheung.trading.model.Order;
import com.rkfcheung.trading.model.Side;
import com.rkfcheung.trading.repository.OrderBook;
import com.rkfcheung.trading.repository.PriceLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

class MatchingEngineTest {

    private static final UUID INSTRUMENT = UUID.randomUUID();
    private MatchingEngine matchingEngine;
    private OrderBook orderBook;

    @BeforeEach
    void setup() {
        orderBook = mock(OrderBook.class);
        matchingEngine = new MatchingEngine(orderBook);
    }

    @Test
    void testMatchFullyWhenExactLiquidityAvailable() {
        // Given
        var incoming = new Order(UUID.randomUUID(), Side.BID, INSTRUMENT,
                BidPrice.of(new BigDecimal("100")), 10, false, UUID.randomUUID(), Instant.now());
        var resting = new Order(UUID.randomUUID(), Side.ASK, INSTRUMENT,
                AskPrice.of(new BigDecimal("100")), 10, false, UUID.randomUUID(), Instant.now());

        var priceLevel = new PriceLevel(Side.ASK);
        priceLevel.add(resting);

        // When
        when(orderBook.priceLevel(INSTRUMENT, Side.ASK)).thenReturn(priceLevel);
        when(orderBook.execute(any(), anyDouble())).thenReturn(Mono.just(true));

        // Then
        StepVerifier.create(matchingEngine.match(incoming))
                .expectNextMatches(result ->
                        result.orderId().equals(incoming.id()) &&
                                result.executionPrice().compareTo(new BigDecimal("100.00000000")) == 0)
                .verifyComplete();

        verify(orderBook).execute(resting.id(), 100.0);
    }

    @Test
    void testNotMatchWhenRestingQuantityTooLarge() {
        // Given
        var incoming = new Order(UUID.randomUUID(), Side.BID, INSTRUMENT,
                BidPrice.of(new BigDecimal("100")), 5, false, UUID.randomUUID(), Instant.now());
        var resting = new Order(UUID.randomUUID(), Side.ASK, INSTRUMENT,
                AskPrice.of(new BigDecimal("100")), 10, false, UUID.randomUUID(), Instant.now());

        var priceLevel = new PriceLevel(Side.ASK);
        priceLevel.add(resting);

        // When
        when(orderBook.priceLevel(INSTRUMENT, Side.ASK)).thenReturn(priceLevel);

        // Then
        StepVerifier.create(matchingEngine.match(incoming))
                .expectNextMatches(result -> result.executionPrice() == null)
                .verifyComplete();

        verify(orderBook, never()).execute(any(), anyDouble());
    }

    @Test
    void shouldMatchMarketOrderWithLimitResting() {
        // Given
        var incoming = new Order(UUID.randomUUID(), Side.BID, INSTRUMENT,
                BidPrice.of(null), 10, true, UUID.randomUUID(), Instant.now());
        var resting = new Order(UUID.randomUUID(), Side.ASK, INSTRUMENT,
                AskPrice.of(new BigDecimal("102.50")), 10, false, UUID.randomUUID(), Instant.now());

        var priceLevel = new PriceLevel(Side.ASK);
        priceLevel.add(resting);

        // When
        when(orderBook.priceLevel(INSTRUMENT, Side.ASK)).thenReturn(priceLevel);
        when(orderBook.execute(any(), anyDouble())).thenReturn(Mono.just(true));

        // Then
        StepVerifier.create(matchingEngine.match(incoming))
                .expectNextMatches(result ->
                        result.executionPrice().compareTo(new BigDecimal("102.50000000")) == 0)
                .verifyComplete();

        verify(orderBook).execute(resting.id(), 102.5);
    }

    @Test
    void testNotMatchMarketAgainstMarket() {
        // Given
        var incoming = new Order(UUID.randomUUID(), Side.BID, INSTRUMENT,
                BidPrice.of(null), 10, true, UUID.randomUUID(), Instant.now());
        var resting = new Order(UUID.randomUUID(), Side.ASK, INSTRUMENT,
                AskPrice.of(null), 10, true, UUID.randomUUID(), Instant.now());

        var priceLevel = new PriceLevel(Side.ASK);
        priceLevel.add(resting);

        // When
        when(orderBook.priceLevel(INSTRUMENT, Side.ASK)).thenReturn(priceLevel);

        // Then
        StepVerifier.create(matchingEngine.match(incoming))
                .expectNextMatches(result -> result.executionPrice() == null)
                .verifyComplete();

        verify(orderBook, never()).execute(any(), anyDouble());
    }
}
