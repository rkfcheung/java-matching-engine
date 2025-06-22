package com.rkfcheung.trading.model;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;

public record BidPrice(BigDecimal value) implements Price {

    @Override
    public int compareTo(@NonNull Price other) {
        if (!(other instanceof BidPrice)) {
            throw new IllegalArgumentException("Cannot compare BidPrice with non-BidPrice");
        }

        if (this.isMarketOrder() && !other.isMarketOrder()) return -1;
        if (!this.isMarketOrder() && other.isMarketOrder()) return 1;
        if (this.isMarketOrder() && other.isMarketOrder()) return 0;

        return other.value().compareTo(this.value());
    }
}
