package com.rkfcheung.trading.service;

import com.rkfcheung.trading.api.NewRequest;
import com.rkfcheung.trading.common.Result;
import com.rkfcheung.trading.error.ValidationError;
import org.springframework.lang.NonNull;

public class ValidationService {

    public Result valid(@NonNull NewRequest request) {
        if (request.price() != null && request.price() <= 0.0) {
            return new Result.Err<>(ValidationError.INVALID_PRICE);
        }

        if (request.quantity() <= 0L) {
            return new Result.Err<>(ValidationError.INVALID_QUANTITY);
        }

        return new Result.Ok<>(null);
    }
}
