package com.rkfcheung.trading.model;

import com.rkfcheung.trading.api.NewRequest;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.UUID;

public record Order(
        @NonNull UUID id,
        @NonNull Side side,
        @NonNull UUID instrumentId,
        @NonNull Price price,
        long quantity,
        boolean isMarketOrder,
        @NonNull UUID clientId,
        @NonNull Instant createdAt
) {

    @NonNull
    public static Order of(@NonNull UUID clientId, @NonNull NewRequest request) {
        var side = switch (request.orderType()) {
            case BUY -> Side.BID;
            case SELL -> Side.ASK;
        };
        var price = Price.of(side, request.price());

        return new Order(UUID.randomUUID(), side, request.instrumentId(), price, request.quantity(), price.isMarketOrder(), clientId, Instant.now());
    }
}
