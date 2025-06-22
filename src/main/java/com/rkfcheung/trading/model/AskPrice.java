package com.rkfcheung.trading.model;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;

public record AskPrice(BigDecimal value) implements Price, Comparable<AskPrice> {
    
    @Override
    public int compareTo(@NonNull AskPrice other) {

        if (this.isMarketOrder() && !other.isMarketOrder()) return -1;
        if (!this.isMarketOrder() && other.isMarketOrder()) return 1;
        if (this.isMarketOrder() && other.isMarketOrder()) return 0;

        return this.value().compareTo(other.value());
    }
}
