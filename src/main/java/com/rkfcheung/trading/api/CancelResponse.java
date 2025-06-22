package com.rkfcheung.trading.api;

import org.springframework.lang.Nullable;

public record CancelResponse(boolean success, @Nullable String reason) {
}
