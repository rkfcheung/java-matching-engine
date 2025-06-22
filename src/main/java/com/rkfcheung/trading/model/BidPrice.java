package com.rkfcheung.trading.model;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;

public record BidPrice(BigDecimal value) implements Price, Comparable<BidPrice> {

    @Override
    public int compareTo(@NonNull BidPrice other) {
        if (this.isMarketOrder() && !other.isMarketOrder()) return -1;
        if (!this.isMarketOrder() && other.isMarketOrder()) return 1;
        if (this.isMarketOrder() && other.isMarketOrder()) return 0;

        return other.value().compareTo(this.value());
    }
}
