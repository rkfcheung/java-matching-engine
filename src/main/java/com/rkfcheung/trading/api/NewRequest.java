package com.rkfcheung.trading.api;

import org.springframework.lang.Nullable;

import java.util.UUID;

public record NewRequest(OrderType orderType, UUID instrumentId, @Nullable Double price, long quantity) {
}
