package com.rkfcheung.trading.model;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;

public record AskPrice(BigDecimal value) implements Price {

    @NonNull
    public static AskPrice of(BigDecimal value) {
        return new AskPrice(value);
    }

    @Override
    public Side side() {
        return Side.ASK;
    }

    @Override
    public int compareTo(@NonNull Price other) {
        if (!(other instanceof AskPrice)) {
            throw new IllegalArgumentException("Cannot compare AskPrice with non-AskPrice");
        }

        if (this.isMarketOrder() && !other.isMarketOrder()) return -1;
        if (!this.isMarketOrder() && other.isMarketOrder()) return 1;
        if (this.isMarketOrder() && other.isMarketOrder()) return 0;

        return this.value().compareTo(other.value());
    }
}
