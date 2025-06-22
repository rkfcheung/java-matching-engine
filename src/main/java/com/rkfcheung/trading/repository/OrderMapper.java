package com.rkfcheung.trading.repository;

import com.rkfcheung.trading.api.OrderStatus;
import com.rkfcheung.trading.model.AskPrice;
import com.rkfcheung.trading.model.BidPrice;
import com.rkfcheung.trading.model.Order;
import com.rkfcheung.trading.model.Price;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class OrderMapper {

    @NonNull
    public OrderEntity toEntity(@NonNull Order order) {
        var price = Optional.ofNullable(order.price())
                .map(Price::value)
                .map(BigDecimal::doubleValue)
                .orElse(null);

        return new OrderEntity(
                order.id(),
                order.clientId(),
                order.instrumentId(),
                price,
                order.quantity(),
                order.side(),
                order.isMarketOrder(),
                OrderStatus.PENDING,
                null,
                order.createdAt(),
                null,
                null
        );
    }

    @NonNull
    public Order toDomain(@NonNull OrderEntity entity) {
        Price price = null;
        if (entity.getPrice() != null) {
            var bigDecimal = BigDecimal.valueOf(entity.getPrice());
            price = switch (entity.getSide()) {
                case BID -> new BidPrice(bigDecimal);
                case ASK -> new AskPrice(bigDecimal);
            };
        }

        return new Order(
                entity.getId(),
                entity.getSide(),
                entity.getInstrumentId(),
                price,
                entity.getQuantity(),
                entity.isMarketOrder(),
                entity.getClientId(),
                entity.getCreatedAt()
        );
    }
}

