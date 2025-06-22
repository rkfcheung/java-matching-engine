package com.rkfcheung.trading.model;

import com.rkfcheung.trading.api.NewRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.UUID;

public record Order(
        @NonNull UUID id,
        @NonNull Side side,
        @NonNull UUID instrumentId,
        @Nullable Double price,
        long quantity,
        boolean isMarketOrder,
        @NonNull UUID clientId
) {

    @NonNull
    public static Order of(@NonNull UUID clientId, @NonNull NewRequest request) {
        var orderType = request.orderType();
        var side = switch (orderType) {
            case BUY -> Side.BID;
            case SELL -> Side.ASK;
        };
        final boolean isMarketOrder = request.price() == null;

        return new Order(UUID.randomUUID(), side, request.instrumentId(), request.price(), request.quantity(), isMarketOrder, clientId);
    }
}
