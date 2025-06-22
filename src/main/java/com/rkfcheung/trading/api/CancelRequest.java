package com.rkfcheung.trading.api;

import java.util.UUID;

public record CancelRequest(UUID orderId) {
}
