package com.rkfcheung.trading.api;

import com.rkfcheung.trading.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Mono<NewResponse> add(
            @RequestHeader("client-id") UUID clientId,
            @RequestBody NewRequest request) {
        log.info("New order from client {}: {}", clientId, request);
        return orderService.add(clientId, request);
    }

    @PutMapping("/{orderId}")
    public Mono<CancelResponse> cancel(
            @RequestHeader("client-id") UUID clientId,
            @PathVariable UUID orderId
    ) {
        log.info("Cancel request from client {} for order {}", clientId, orderId);
        return orderService.cancel(clientId, orderId);
    }
}
