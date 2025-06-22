package com.rkfcheung.trading.api;

import java.time.Instant;
import java.util.UUID;

public record NewResponse(UUID orderId, OrderStatus orderStatus, Instant timestamp) {
}
