package com.rkfcheung.trading.api;

import com.rkfcheung.trading.error.ValidationError;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.UUID;

public record NewResponse(
        @Nullable UUID orderId,
        @NonNull OrderStatus orderStatus,
        @Nullable Double executionPrice,
        @NonNull Instant timestamp,
        @Nullable ValidationError validationError
) {

    @NonNull
    public static NewResponse rejected(ValidationError error) {
        return new NewResponse(null, OrderStatus.REJECTED, null, Instant.now(), error);
    }
}
