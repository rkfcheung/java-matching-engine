package com.rkfcheung.trading.service;

import com.rkfcheung.trading.api.CancelResponse;
import com.rkfcheung.trading.api.NewRequest;
import com.rkfcheung.trading.api.NewResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class SimpleOrderService implements OrderService {
    
    @Override
    public Mono<NewResponse> add(UUID clientId, NewRequest request) {
        return Mono.empty();
    }

    @Override
    public Mono<CancelResponse> cancel(UUID clientId, UUID orderId) {
        return Mono.empty();
    }
}
