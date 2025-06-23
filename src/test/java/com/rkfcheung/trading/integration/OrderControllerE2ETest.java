package com.rkfcheung.trading.integration;

import com.rkfcheung.trading.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class OrderControllerE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void createThenCancelOrder() {
        var clientId = UUID.randomUUID();
        var instrumentId = UUID.randomUUID();
        var newOrder = new NewRequest(
                OrderType.BUY,
                instrumentId,
                100.0,
                10
        );

        var response = webTestClient.post()
                .uri("/orders")
                .header("client-id", clientId.toString())
                .bodyValue(newOrder)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        var orderId = response.orderId();
        assertThat(orderId).isNotNull();

        var cancelResponse = webTestClient.put()
                .uri("/orders/{orderId}", orderId)
                .header("client-id", clientId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CancelResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(cancelResponse).isNotNull();
        assertThat(cancelResponse.success()).isTrue();
        assertThat(cancelResponse.reason()).isNull();
    }

    @Test
    void createMarketOrderWithoutPrice() {
        var clientId = UUID.randomUUID();
        var instrumentId = UUID.randomUUID();
        var newOrder = new NewRequest(
                OrderType.BUY,
                instrumentId,
                null,
                5
        );

        var response = webTestClient.post()
                .uri("/orders")
                .header("client-id", clientId.toString())
                .bodyValue(newOrder)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isNotNull();
    }

    @Test
    void rejectOrderWithMissingFields() {
        var clientId = UUID.randomUUID();
        var invalidOrder = """
                {
                  "quantity": 10
                }
                """;

        webTestClient.post()
                .uri("/orders")
                .header("client-id", clientId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidOrder)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void cancelNonExistentOrderShouldFail() {
        var clientId = UUID.randomUUID();
        var fakeOrderId = UUID.randomUUID();

        var cancelResponse = webTestClient.put()
                .uri("/orders/{orderId}", fakeOrderId)
                .header("client-id", clientId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CancelResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(cancelResponse).isNotNull();
        assertThat(cancelResponse.success()).isFalse();
        assertThat(cancelResponse.reason()).isNotEmpty();
    }

    @Test
    void matchBuyAndSellOrder() {
        var instrumentId = UUID.randomUUID();

        var buyClient = UUID.randomUUID();
        var sellClient = UUID.randomUUID();

        var buyRequest = new NewRequest(OrderType.BUY, instrumentId, 100.0, 10);
        var sellRequest = new NewRequest(OrderType.SELL, instrumentId, 100.0, 10);

        var buyResponse = webTestClient.post()
                .uri("/orders")
                .header("client-id", buyClient.toString())
                .bodyValue(buyRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(buyResponse).isNotNull();
        assertThat(buyResponse.orderId()).isNotNull();
        assertThat(buyResponse.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(buyResponse.executionPrice()).isNull();

        var sellResponse = webTestClient.post()
                .uri("/orders")
                .header("client-id", sellClient.toString())
                .bodyValue(sellRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(sellResponse).isNotNull();
        assertThat(sellResponse.orderId()).isNotNull();
        assertThat(sellResponse.orderStatus()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(sellResponse.executionPrice()).isEqualTo(100.0);
    }

    @Test
    void failMatchDueToInsufficientLiquidity() {
        var instrumentId = UUID.randomUUID();
        var buyClient = UUID.randomUUID();
        var sellClient = UUID.randomUUID();

        var sellRequest = new NewRequest(OrderType.SELL, instrumentId, 100.0, 5);
        var sellResponse = webTestClient.post()
                .uri("/orders")
                .header("client-id", sellClient.toString())
                .bodyValue(sellRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(sellResponse).isNotNull();
        assertThat(sellResponse.orderId()).isNotNull();
        assertThat(sellResponse.orderStatus()).isEqualTo(OrderStatus.PENDING);

        var buyRequest = new NewRequest(OrderType.BUY, instrumentId, 100.0, 10);
        var buyResponse = webTestClient.post()
                .uri("/orders")
                .header("client-id", buyClient.toString())
                .bodyValue(buyRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(buyResponse).isNotNull();
        assertThat(buyResponse.orderId()).isNotNull();
        assertThat(buyResponse.orderStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void failMatchDueToPartialFillRejection() {
        var instrumentId = UUID.randomUUID();
        var buyClient = UUID.randomUUID();
        var sellClient = UUID.randomUUID();

        var sellRequest = new NewRequest(OrderType.SELL, instrumentId, 100.0, 20);
        var sellResponse = webTestClient.post()
                .uri("/orders")
                .header("client-id", sellClient.toString())
                .bodyValue(sellRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(sellResponse).isNotNull();
        assertThat(sellResponse.orderStatus()).isEqualTo(OrderStatus.PENDING);

        var buyRequest = new NewRequest(OrderType.BUY, instrumentId, 100.0, 10);
        var buyResponse = webTestClient.post()
                .uri("/orders")
                .header("client-id", buyClient.toString())
                .bodyValue(buyRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(buyResponse).isNotNull();
        assertThat(buyResponse.orderId()).isNotNull();
        assertThat(buyResponse.orderStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void rejectOrderWithInvalidQuantity() {
        var clientId = UUID.randomUUID();
        var instrumentId = UUID.randomUUID();
        var invalidOrder = new NewRequest(OrderType.BUY, instrumentId, 100.0, 0);

        webTestClient.post()
                .uri("/orders")
                .header("client-id", clientId.toString())
                .bodyValue(invalidOrder)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void matchMarketBuyWithRestingSell() {
        var instrumentId = UUID.randomUUID();
        var buyClient = UUID.randomUUID();
        var sellClient = UUID.randomUUID();

        var sellRequest = new NewRequest(OrderType.SELL, instrumentId, 100.0, 10);
        var sellResponse = webTestClient.post()
                .uri("/orders")
                .header("client-id", sellClient.toString())
                .bodyValue(sellRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(sellResponse).isNotNull();
        assertThat(sellResponse.orderStatus()).isEqualTo(OrderStatus.PENDING);

        var buyRequest = new NewRequest(OrderType.BUY, instrumentId, null, 10);
        var buyResponse = webTestClient.post()
                .uri("/orders")
                .header("client-id", buyClient.toString())
                .bodyValue(buyRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(buyResponse).isNotNull();
        assertThat(buyResponse.orderId()).isNotNull();
        assertThat(buyResponse.orderStatus()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(buyResponse.executionPrice()).isEqualTo(100.0);
    }
}
