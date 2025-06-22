package com.rkfcheung.trading.service;

import com.rkfcheung.trading.model.MatchResult;
import com.rkfcheung.trading.model.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MatchingEngine {

    public Mono<MatchResult> match(Order order) {
        return Mono.just(new MatchResult(order.id(), null));
    }
}
