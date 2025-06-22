package com.rkfcheung.trading.service;

import com.rkfcheung.trading.api.NewRequest;
import com.rkfcheung.trading.api.OrderType;
import com.rkfcheung.trading.common.Result;
import com.rkfcheung.trading.error.ValidationError;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationServiceTest {

    private final ValidationService validationService = new ValidationService();

    @Test
    void validReturnsOkForValidRequest() {
        NewRequest request = new NewRequest(
                OrderType.BUY,
                UUID.randomUUID(),
                100.0,
                10L
        );

        Result result = validationService.valid(request);

        assertThat(result.isOk()).isEqualTo(true);
        assertThat(result.isErr()).isEqualTo(false);
    }

    @Test
    void validReturnsOkForValidRequestWithMarketOrder() {
        NewRequest request = new NewRequest(
                OrderType.BUY,
                UUID.randomUUID(),
                null,
                10L
        );

        Result result = validationService.valid(request);

        assertThat(result.isOk()).isEqualTo(true);
    }

    @Test
    void validReturnsErrForInvalidPrice() {
        NewRequest request = new NewRequest(
                OrderType.SELL,
                UUID.randomUUID(),
                -1.0,
                10L
        );

        Result result = validationService.valid(request);

        assertThat(result.isOk()).isEqualTo(false);
        assertThat(result.isErr()).isEqualTo(true);
        assertThat(((Result.Err<?>) result).error())
                .isEqualTo(ValidationError.INVALID_PRICE);
    }

    @Test
    void validReturnsErrForZeroQuantity() {
        NewRequest request = new NewRequest(
                OrderType.BUY,
                UUID.randomUUID(),
                100.0,
                0L
        );

        Result result = validationService.valid(request);

        assertThat(result.isErr()).isEqualTo(true);
        assertThat(((Result.Err<?>) result).error())
                .isEqualTo(ValidationError.INVALID_QUANTITY);
    }

    @Test
    void validReturnsErrForNegativeQuantity() {
        NewRequest request = new NewRequest(
                OrderType.SELL,
                UUID.randomUUID(),
                100.0,
                -5L
        );

        Result result = validationService.valid(request);

        assertThat(result.isErr()).isEqualTo(true);
        assertThat(((Result.Err<?>) result).error())
                .isEqualTo(ValidationError.INVALID_QUANTITY);
    }
}
