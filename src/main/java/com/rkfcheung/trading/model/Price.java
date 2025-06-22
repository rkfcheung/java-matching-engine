package com.rkfcheung.trading.model;

import java.math.BigDecimal;

public sealed interface Price permits BidPrice, AskPrice {
    BigDecimal value();

    default boolean isMarketOrder() {
        return value() == null;
    }
}
