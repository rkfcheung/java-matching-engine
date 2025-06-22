package com.rkfcheung.trading.repository;

import com.rkfcheung.trading.api.OrderStatus;
import com.rkfcheung.trading.model.Side;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class OrderEntity {

    @Id
    private UUID id;
    private UUID clientId;
    private UUID instrumentId;
    private Double price;
    private long quantity;
    private Side side;
    private boolean isMarketOrder;
    private OrderStatus orderStatus;
    private Double executionPrice;
    private Instant createdAt;
    private Instant executedAt;
    private Instant cancelledAt;
}

