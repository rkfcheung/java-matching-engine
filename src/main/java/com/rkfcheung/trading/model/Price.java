package com.rkfcheung.trading.model;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.Optional;

public sealed interface Price extends Comparable<Price> permits BidPrice, AskPrice {
    @NonNull
    static Price of(@NonNull Side side, Double value) {
        var priceValue = Optional.ofNullable(value)
                .map(v -> new BigDecimal(v.toString()))
                .orElse(null);
        return switch (side) {
            case BID -> BidPrice.of(priceValue);
            case ASK -> AskPrice.of(priceValue);
        };
    }

    Side side();

    BigDecimal value();

    default boolean isMarketOrder() {
        return value() == null;
    }
}
