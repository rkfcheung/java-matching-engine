package com.rkfcheung.trading.model;

import java.math.BigDecimal;
import java.util.UUID;

public record MatchResult(UUID orderId, BigDecimal executionPrice) {
}
