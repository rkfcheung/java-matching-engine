package com.rkfcheung.trading.api;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.UUID;

public record NewRequest(
        @NonNull OrderType orderType,
        @NonNull UUID instrumentId,
        @Nullable Double price,
        long quantity
) {
}
