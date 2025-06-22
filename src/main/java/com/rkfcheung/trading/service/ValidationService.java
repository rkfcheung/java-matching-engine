package com.rkfcheung.trading.service;

import com.rkfcheung.trading.api.NewRequest;
import com.rkfcheung.trading.error.ValidationError;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValidationService {

    public Optional<ValidationError> valid(@NonNull NewRequest request) {
        if (request.price() != null && request.price() <= 0.0) {
            return Optional.of(ValidationError.INVALID_PRICE);
        }

        if (request.quantity() <= 0L) {
            return Optional.of(ValidationError.INVALID_QUANTITY);
        }

        return Optional.empty();
    }
}
