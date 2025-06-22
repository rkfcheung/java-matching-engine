package com.rkfcheung.trading.repository;

import com.rkfcheung.trading.model.Order;
import com.rkfcheung.trading.model.Side;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class OrderBook {

    private final Map<UUID, PriceLevel> bids = new ConcurrentHashMap<>();
    private final Map<UUID, PriceLevel> asks = new ConcurrentHashMap<>();
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public Mono<Order> add(@NonNull Order order) {
        var priceLevel = priceLevel(order.instrumentId(), order.side());
        return Mono.justOrEmpty(priceLevel.add(order))
                .map(orderMapper::toEntity)
                .flatMap(orderRepository::insert)
                .map(orderMapper::toDomain);
    }

    public Mono<Instant> cancel(UUID clientId, UUID orderId) {
        return orderRepository.find(orderId, clientId)
                .flatMap(entity -> {
                    var order = orderMapper.toDomain(entity);
                    var priceLevel = priceLevel(order.instrumentId(), order.side());
                    if (priceLevel.remove(order)) {
                        return orderRepository.cancel(orderId);
                    } else {
                        return Mono.empty();
                    }
                });
    }

    private PriceLevel priceLevel(UUID instrumentId, @NonNull Side side) {
        var orderBook = switch (side) {
            case BID -> bids;
            case ASK -> asks;
        };
        return orderBook.computeIfAbsent(instrumentId, _k -> new PriceLevel(side));
    }
}
