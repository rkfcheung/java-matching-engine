package com.rkfcheung.trading.service;

import com.rkfcheung.trading.model.MatchResult;
import com.rkfcheung.trading.model.Order;
import reactor.core.publisher.Mono;

public interface MatchingEngine {

    Mono<MatchResult> match(Order order);
}
