package com.rkfcheung.trading.service;

import com.rkfcheung.trading.model.*;
import com.rkfcheung.trading.repository.OrderBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

    private static final int SCALE = 8;
    private final OrderBook orderBook;

    public Mono<MatchResult> match(@NonNull Order incomingOrder) {
        var oppositeSide = incomingOrder.side().flip();
        var priceLevel = orderBook.priceLevel(incomingOrder.instrumentId(), oppositeSide);
        var requiredQty = incomingOrder.quantity();
        var executionCost = BigDecimal.ZERO;
        var matchedOrders = new ArrayList<Order>();
        var remaining = requiredQty;

        for (var entry : priceLevel.entrySet()) {
            var price = entry.getKey();
            var queue = entry.getValue();

            for (var restingOrder : queue) {
                if (!canMatch(incomingOrder.price(), price)) {
                    continue;
                }

                var restingQty = restingOrder.quantity();
                if (restingQty > remaining) {
                    log.info("Order {} not matched: resting order quantity {} exceeds remaining {}",
                            incomingOrder.id(), restingQty, remaining);
                    return Mono.just(new MatchResult(incomingOrder.id(), null));
                }

                matchedOrders.add(restingOrder);
                remaining -= restingQty;
                var matchingPrice = calcMatchingPrice(incomingOrder.price(), price);
                var cost = matchingPrice.multiply(BigDecimal.valueOf(restingQty));
                executionCost = executionCost.add(cost);

                if (remaining == 0) {
                    break;
                }
            }

            if (remaining == 0) {
                break;
            }
        }

        if (remaining > 0) {
            log.info("Order {} not matched: insufficient liquidity, remaining quantity {}",
                    incomingOrder.id(), remaining);
            return Mono.just(new MatchResult(incomingOrder.id(), null));
        }

        var executionPrice = executionCost.divide(BigDecimal.valueOf(requiredQty), SCALE, RoundingMode.HALF_UP);
        return Flux.fromIterable(matchedOrders)
                .flatMap(matched -> {
                    if (priceLevel.remove(matched)) {
                        return orderBook.execute(matched.id(), executionPrice.doubleValue());
                    } else {
                        return Mono.just(false);
                    }
                })
                .then(Mono.just(new MatchResult(incomingOrder.id(), executionPrice)));
    }

    private boolean canMatch(@NonNull Price incomingPrice, @NonNull Price restingPrice) {
        if (incomingPrice.isMarketOrder() && restingPrice.isMarketOrder() || incomingPrice.side() == restingPrice.side()) {
            return false;
        }

        if (incomingPrice.isMarketOrder() || restingPrice.isMarketOrder()) {
            return true;
        }

        return switch (incomingPrice) {
            case BidPrice bidPrice -> {
                var askPrice = (AskPrice) restingPrice;
                yield bidPrice.value().compareTo(askPrice.value()) >= 0;
            }
            case AskPrice askPrice -> {
                var bidPrice = (BidPrice) restingPrice;
                yield askPrice.value().compareTo(bidPrice.value()) <= 0;
            }
        };
    }

    private BigDecimal calcMatchingPrice(@NonNull Price incomingPrice, @NonNull Price restingPrice) {
        if (incomingPrice.isMarketOrder()) {
            return restingPrice.value();
        } else if (restingPrice.isMarketOrder()) {
            return incomingPrice.value();
        } else {
            return restingPrice.value();
        }
    }
}
