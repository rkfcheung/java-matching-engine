package com.rkfcheung.trading.repository;

import com.rkfcheung.trading.model.Order;
import com.rkfcheung.trading.model.Price;
import com.rkfcheung.trading.model.Side;
import org.springframework.lang.NonNull;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class PriceLevel {

    private final Side side;
    private final TreeMap<Price, ArrayDeque<Order>> book;

    public PriceLevel(Side side) {
        this.side = side;
        this.book = new TreeMap<>();
    }

    public Optional<Order> add(Order order) {
        if (isInvalid(order)) {
            return Optional.empty();
        }

        update(order);

        return Optional.of(order);
    }

    public Optional<Order> best() {
        return Optional.ofNullable(book.firstEntry())
                .map(Map.Entry::getValue)
                .map(ArrayDeque::peek);
    }

    public boolean isEmpty() {
        return book.isEmpty();
    }

    public Optional<Order> pop() {
        if (book.isEmpty()) {
            return Optional.empty();
        }

        var entry = book.firstEntry();
        var queue = entry.getValue();
        var order = queue.poll();

        if (queue.isEmpty()) {
            book.remove(entry.getKey());
        }

        return Optional.ofNullable(order);
    }

    public boolean remove(@NonNull Order order) {
        var queue = book.get(order.price());
        if (queue == null) {
            return false;
        }

        var removed = queue.removeIf(o -> o.id().equals(order.id()));
        if (queue.isEmpty()) {
            book.remove(order.price());
        }

        return removed;
    }


    private boolean isInvalid(@NonNull Order order) {
        return order.side() != side;
    }

    private void update(@NonNull Order order) {
        book.computeIfAbsent(order.price(), _k -> new ArrayDeque<>()).add(order);
    }
}
