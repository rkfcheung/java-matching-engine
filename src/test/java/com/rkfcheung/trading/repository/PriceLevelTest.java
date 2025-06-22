package com.rkfcheung.trading.repository;

import com.rkfcheung.trading.model.AskPrice;
import com.rkfcheung.trading.model.BidPrice;
import com.rkfcheung.trading.model.Order;
import com.rkfcheung.trading.model.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PriceLevelTest {

    private PriceLevel bidBook;
    private PriceLevel askBook;
    private UUID clientId;
    private UUID instrumentId;

    @BeforeEach
    void setUp() {
        bidBook = new PriceLevel(Side.BID);
        askBook = new PriceLevel(Side.ASK);
        clientId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
    }

    @Test
    void testAddAndBestBid() {
        var bid1 = newBid(100.0);
        var bid2 = newBid(101.0);

        bidBook.add(bid1);
        bidBook.add(bid2);

        var best = bidBook.best();
        assertTrue(best.isPresent());
        assertEquals(bid2.id(), best.get().id());
    }

    @Test
    void testAddAndBestAsk() {
        var ask1 = newAsk(100.0);
        var ask2 = newAsk(99.0);

        askBook.add(ask1);
        askBook.add(ask2);

        var best = askBook.best();
        assertTrue(best.isPresent());
        assertEquals(ask2.id(), best.get().id());
    }

    @Test
    void testInvalidOrderIsRejected() {
        var ask = newAsk(100.0);
        var result = bidBook.add(ask);
        assertTrue(result.isEmpty());
    }

    @Test
    void testPopRemovesAndReturnsBest() {
        var ask1 = newAsk(101.0);
        var ask2 = newAsk(100.0);

        askBook.add(ask1);
        askBook.add(ask2);

        var first = askBook.pop();
        assertTrue(first.isPresent());
        assertEquals(ask2.id(), first.get().id());

        var second = askBook.pop();
        assertTrue(second.isPresent());
        assertEquals(ask1.id(), second.get().id());

        assertTrue(askBook.isEmpty());
    }

    @Test
    void testIsEmptyInitiallyAndAfterPop() {
        assertTrue(bidBook.isEmpty());

        var bid = newBid(100.0);
        bidBook.add(bid);

        assertFalse(bidBook.isEmpty());

        bidBook.pop();
        assertTrue(bidBook.isEmpty());
    }

    @Test
    void testMarketOrderComesBeforeLimitOrder() {
        var marketBid = newMarketBid();
        var limitBid = newBid(100.0);

        bidBook.add(limitBid);
        bidBook.add(marketBid);

        var first = bidBook.pop();
        assertTrue(first.isPresent());
        assertEquals(marketBid.id(), first.get().id());

        var second = bidBook.pop();
        assertTrue(second.isPresent());
        assertEquals(limitBid.id(), second.get().id());

        assertTrue(bidBook.isEmpty());
    }

    @Test
    void testFifoWithinMarketOrders() {
        var market1 = newMarketBid();
        var market2 = newMarketBid();

        bidBook.add(market1);
        bidBook.add(market2);

        var first = bidBook.pop();

        assertTrue(first.isPresent());
        assertEquals(market1.id(), first.get().id());

        var second = bidBook.pop();
        assertTrue(second.isPresent());
        assertEquals(market2.id(), second.get().id());
    }

    @NonNull
    private Order newBid(double price) {
        return new Order(UUID.randomUUID(), Side.BID, instrumentId, new BidPrice(BigDecimal.valueOf(price)), 10, false, clientId);
    }

    @NonNull
    private Order newAsk(double price) {
        return new Order(UUID.randomUUID(), Side.ASK, instrumentId, new AskPrice(BigDecimal.valueOf(price)), 10, false, clientId);
    }

    @NonNull
    private Order newMarketBid() {
        return new Order(UUID.randomUUID(), Side.BID, instrumentId, new BidPrice(null), 10, true, clientId);
    }
}
