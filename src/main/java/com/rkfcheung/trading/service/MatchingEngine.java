package com.rkfcheung.trading.service;

import com.rkfcheung.trading.model.*;
import com.rkfcheung.trading.repository.OrderBook;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MatchingEngine {

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

                if (restingOrder.quantity() > remaining) {
                    return Mono.just(new MatchResult(incomingOrder.id(), null));
                }

                matchedOrders.add(restingOrder);
                remaining -= restingOrder.quantity();
                var matchingPrice = calcMatchingPrice(incomingOrder.price(), price);
                var cost = matchingPrice.multiply(BigDecimal.valueOf(restingOrder.quantity()));
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
            return Mono.just(new MatchResult(incomingOrder.id(), null));
        }

        for (Order matched : matchedOrders) {
            priceLevel.remove(matched);
        }

        var executionPrice = executionCost.divide(BigDecimal.valueOf(requiredQty), RoundingMode.HALF_UP);

        return Mono.just(new MatchResult(incomingOrder.id(), executionPrice));
    }

    private boolean canMatch(@NonNull Price incomingPrice, @NonNull Price restingPrice) {
        if (incomingPrice.isMarketOrder() && restingPrice.isMarketOrder()) {
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
