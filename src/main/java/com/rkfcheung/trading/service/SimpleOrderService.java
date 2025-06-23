package com.rkfcheung.trading.service;

import com.rkfcheung.trading.api.CancelResponse;
import com.rkfcheung.trading.api.NewRequest;
import com.rkfcheung.trading.api.NewResponse;
import com.rkfcheung.trading.api.OrderStatus;
import com.rkfcheung.trading.model.Order;
import com.rkfcheung.trading.repository.OrderBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleOrderService implements OrderService {

    private final ValidationService validationService;
    private final MatchingEngine matchingEngine;
    private final OrderBook orderBook;

    @Override
    public Mono<NewResponse> add(UUID clientId, NewRequest request) {
        return validationService.valid(request)
                .map(error -> Mono.<NewResponse>error(error.asException()))
                .orElseGet(() -> {
                    var order = Order.of(clientId, request);
                    return orderBook.add(order)
                            .flatMap(matchingEngine::match)
                            .map(result -> new NewResponse(
                                    order.id(),
                                    result.executionPrice() == null ? OrderStatus.PENDING : OrderStatus.EXECUTED,
                                    Optional.ofNullable(result.executionPrice()).map(BigDecimal::doubleValue).orElse(null),
                                    Instant.now(),
                                    null
                            ));
                });
    }

    @Override
    public Mono<CancelResponse> cancel(UUID clientId, UUID orderId) {
        return orderBook.cancel(clientId, orderId)
                .map(order -> new CancelResponse(true, null))
                .switchIfEmpty(Mono.just(new CancelResponse(false, "Order not found or not owned by client")));
    }
}
