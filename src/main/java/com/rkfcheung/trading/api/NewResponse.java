package com.rkfcheung.trading.api;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.UUID;

public record NewResponse(
        @Nullable UUID orderId,
        @NonNull OrderStatus orderStatus,
        @NonNull Instant timestamp
) {
}
