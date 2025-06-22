package com.rkfcheung.trading.model;

import com.rkfcheung.trading.api.NewRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public record Order(
        @NonNull UUID id,
        @NonNull Side side,
        @NonNull UUID instrumentId,
        @Nullable Price price,
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
        var priceValue = Optional.ofNullable(request.price())
                .map(v -> new BigDecimal(v.toString()))
                .orElse(null);
        var price = switch (orderType) {
            case BUY -> new BidPrice(priceValue);
            case SELL -> new AskPrice(priceValue);
        };
        boolean isMarketOrder = price.isMarketOrder();

        return new Order(UUID.randomUUID(), side, request.instrumentId(), price, request.quantity(), isMarketOrder, clientId);
    }
}
