package com.rkfcheung.trading.service;

import com.rkfcheung.trading.api.CancelResponse;
import com.rkfcheung.trading.api.NewRequest;
import com.rkfcheung.trading.api.NewResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderService {
    Mono<NewResponse> add(UUID clientId, NewRequest request);

    Mono<CancelResponse> cancel(UUID clientId, UUID orderId);
}
