package com.rkfcheung.trading.service;

import com.rkfcheung.trading.api.NewRequest;
import com.rkfcheung.trading.api.OrderStatus;
import com.rkfcheung.trading.api.OrderType;
import com.rkfcheung.trading.error.ValidationError;
import com.rkfcheung.trading.error.ValidationException;
import com.rkfcheung.trading.model.MatchResult;
import com.rkfcheung.trading.model.Order;
import com.rkfcheung.trading.repository.OrderBook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class SimpleOrderServiceTest {

    @Captor
    ArgumentCaptor<Order> orderCaptor;
    @Mock
    private ValidationService validationService;
    @Mock
    private MatchingEngine matchingEngine;
    @Mock
    private OrderBook orderBook;
    @InjectMocks
    private SimpleOrderService simpleOrderService;
    private UUID clientId;
    private NewRequest request;
    private Order order;

    @BeforeEach
    void setUp() {
        openMocks(this);

        clientId = UUID.randomUUID();
        request = new NewRequest(OrderType.BUY, UUID.randomUUID(), 100.0, 10L);
        order = Order.of(clientId, request);
    }

    @Test
    void testAddValidRequestThenExecutesOrder() {
        // Given
        when(validationService.valid(request)).thenReturn(Optional.empty());
        when(orderBook.add(orderCaptor.capture())).thenAnswer(it -> Mono.just(it.getArgument(0)));
        when(matchingEngine.match(orderCaptor.capture()))
                .thenAnswer(it -> {
                    Order o = it.getArgument(0);
                    return Mono.just(new MatchResult(o.id(), new BigDecimal("100.0")));
                });

        // When
        var result = simpleOrderService.add(clientId, request);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertNotNull(response.orderId());
                    return response.orderStatus() == OrderStatus.EXECUTED;
                })
                .verifyComplete();

        var executedOrder = orderCaptor.getValue();
        verify(validationService).valid(request);
        assertThat(orderCaptor.getAllValues()).hasSize(2);
        verify(orderBook).add(executedOrder);
        verify(matchingEngine).match(executedOrder);
        assertThat(executedOrder.instrumentId()).isEqualTo(request.instrumentId());
    }

    @Test
    void testAddInvalidRequestThenReturnsError() {
        // Given
        var invalidRequest = new NewRequest(OrderType.SELL, UUID.randomUUID(), -1.0, 100L);
        when(validationService.valid(invalidRequest))
                .thenReturn(Optional.of(ValidationError.INVALID_PRICE));

        // When
        var result = simpleOrderService.add(clientId, invalidRequest);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).error() == ValidationError.INVALID_PRICE)
                .verify();

        verify(validationService).valid(invalidRequest);
        verifyNoInteractions(orderBook);
        verifyNoInteractions(matchingEngine);
    }

    @Test
    void testCancelExistingOrderThenReturnsSuccess() {
        // Given
        when(orderBook.cancel(clientId, order.id())).thenReturn(Mono.just(Instant.now()));

        // When
        var result = simpleOrderService.cancel(clientId, order.id());

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.success() && response.reason() == null)
                .verifyComplete();

        verify(orderBook).cancel(clientId, order.id());
    }

    @Test
    void testCancelNonExistingOrderThenReturnsFailure() {
        // Given
        when(orderBook.cancel(clientId, order.id())).thenReturn(Mono.empty());

        // When
        var result = simpleOrderService.cancel(clientId, order.id());

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> !response.success() && response.reason() != null)
                .verifyComplete();

        verify(orderBook).cancel(clientId, order.id());
    }
}
