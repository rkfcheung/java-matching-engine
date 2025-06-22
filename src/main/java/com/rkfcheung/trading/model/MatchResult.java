package com.rkfcheung.trading.model;

import java.util.UUID;

public record MatchResult(UUID orderId, Double executionPrice) {
}
