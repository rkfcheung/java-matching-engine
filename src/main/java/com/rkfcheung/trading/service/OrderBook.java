package com.rkfcheung.trading.service;

import com.rkfcheung.trading.model.Order;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderBook {

    Mono<Order> add(Order order);

    Mono<Order> cancel(UUID clientId, UUID orderId);
}
