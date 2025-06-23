package com.rkfcheung.trading.repository;

import com.rkfcheung.trading.api.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final R2dbcEntityTemplate template;

    public Mono<OrderEntity> insert(OrderEntity entity) {
        return template.insert(OrderEntity.class).using(entity);
    }

    public Mono<OrderEntity> find(UUID orderId, UUID clientId) {
        var query = Query.query(
                Criteria.where("id").is(orderId)
                        .and("client_id").is(clientId)
        );
        return template.selectOne(query, OrderEntity.class);
    }

    public Mono<Instant> cancel(UUID orderId) {
        var query = Query.query(Criteria.where("id").is(orderId));
        var cancelledAt = Instant.now();
        var update = Update.update("order_status", OrderStatus.CANCELLED)
                .set("cancelled_at", cancelledAt);
        return template.update(OrderEntity.class)
                .matching(query)
                .apply(update)
                .flatMap(rowsUpdated -> {
                    if (rowsUpdated > 0) {
                        return Mono.just(cancelledAt);
                    } else {
                        return Mono.empty();
                    }
                });
    }

    public Mono<Boolean> execute(UUID orderId, double executionPrice) {
        var query = Query.query(Criteria.where("id").is(orderId));
        var update = Update.update("order_status", OrderStatus.EXECUTED)
                .set("execution_price", executionPrice)
                .set("executed_at", Instant.now());
        return template.update(OrderEntity.class)
                .matching(query)
                .apply(update)
                .map(rowsUpdated -> rowsUpdated > 0);
    }
}
