package com.rkfcheung.trading.api;

import com.rkfcheung.trading.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @Test
    void testAddOrder() {
        var clientId = UUID.randomUUID();
        var instrumentId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var timestamp = Instant.now();
        var request = new NewRequest(OrderType.BUY, instrumentId, 100.5, 10);
        var response = new NewResponse(orderId, OrderStatus.PENDING, timestamp, null);

        when(orderService.add(any(), any())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/orders")
                .header("client-id", clientId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderId.toString())
                .jsonPath("$.orderStatus").isEqualTo("PENDING")
                .jsonPath("$.timestamp").isEqualTo(timestamp.toString());
    }

    @Test
    void testCancelOrder() {
        var clientId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var response = new CancelResponse(true, null);

        when(orderService.cancel(any(), any())).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/orders/" + orderId)
                .header("client-id", clientId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.reason").doesNotExist();
    }
}
