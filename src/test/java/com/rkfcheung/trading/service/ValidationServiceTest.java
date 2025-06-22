package com.rkfcheung.trading.service;

import com.rkfcheung.trading.api.NewRequest;
import com.rkfcheung.trading.api.OrderType;
import com.rkfcheung.trading.error.ValidationError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationServiceTest {

    private final ValidationService validationService = new ValidationService();

    @Test
    void testValidReturnsEmptyWithLimitOrder() {
        var request = new NewRequest(
                OrderType.BUY,
                UUID.randomUUID(),
                100.0,
                10L
        );

        var result = validationService.valid(request);

        assertThat(result).isEqualTo(Optional.empty());
    }

    @Test
    void testValidReturnsEmptyWithMarketOrder() {
        var request = new NewRequest(
                OrderType.BUY,
                UUID.randomUUID(),
                null,
                10L
        );

        var result = validationService.valid(request);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "-1.0, 10, INVALID_PRICE",
            "0.0, 10, INVALID_PRICE",
            "100.0, -5, INVALID_QUANTITY",
            "100.0, 0, INVALID_QUANTITY",
    })
    void testValidReturnsError(double price, long quantity, ValidationError expectedError) {
        var request = new NewRequest(
                OrderType.SELL,
                UUID.randomUUID(),
                price,
                quantity
        );

        var result = validationService.valid(request);

        assertThat(result).isPresent().isEqualTo(Optional.of(expectedError));
    }
}
